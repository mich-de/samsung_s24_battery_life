import socket
import select
import threading
import sys
import time

HOST = "127.0.0.1"
PORT = 8888
BUFFER_SIZE = 65536

total_bytes_transferred = 0
bytes_lock = threading.Lock()

def pipe(source, destination, direction):
    global total_bytes_transferred
    try:
        while True:
            data = source.recv(BUFFER_SIZE)
            if not data:
                break
            destination.sendall(data)
            with bytes_lock:
                total_bytes_transferred += len(data)
    except Exception:
        pass
    finally:
        try:
            source.close()
        except Exception:
            pass
        try:
            destination.close()
        except Exception:
            pass

def handle_client(client_socket, client_addr):
    try:
        request_line = b""
        while b"\r\n" not in request_line:
            chunk = client_socket.recv(1)
            if not chunk:
                return
            request_line += chunk

        req_str = request_line.decode("utf-8", errors="ignore").strip()
        parts = req_str.split()
        if len(parts) < 2:
            client_socket.close()
            return

        method = parts[0]
        target = parts[1]

        # Read remaining headers
        headers = b""
        while b"\r\n\r\n" not in headers:
            chunk = client_socket.recv(1)
            if not chunk:
                break
            headers += chunk

        if method.upper() == "CONNECT":
            # HTTPS tunneling
            if ":" in target:
                host, port_s = target.split(":")
                port = int(port_s)
            else:
                host = target
                port = 443

            print(f"[TUNNEL] HTTPS CONNECT to {host}:{port}")
            sys.stdout.flush()

            server_socket = socket.create_connection((host, port), timeout=15)
            client_socket.sendall(b"HTTP/1.1 200 Connection Established\r\n\r\n")

            t1 = threading.Thread(target=pipe, args=(client_socket, server_socket, "up"), daemon=True)
            t2 = threading.Thread(target=pipe, args=(server_socket, client_socket, "down"), daemon=True)
            t1.start()
            t2.start()
            t1.join()
            t2.join()
        else:
            # Plain HTTP
            if target.startswith("http://"):
                url_without_proto = target[7:]
                slash_idx = url_without_proto.find("/")
                if slash_idx != -1:
                    host_port = url_without_proto[:slash_idx]
                    path = url_without_proto[slash_idx:]
                else:
                    host_port = url_without_proto
                    path = "/"
            else:
                host_port = target
                path = "/"

            if ":" in host_port:
                host, port_s = host_port.split(":")
                port = int(port_s)
            else:
                host = host_port
                port = 80

            print(f"[HTTP] {method} {host}:{port}{path}")
            sys.stdout.flush()

            server_socket = socket.create_connection((host, port), timeout=15)
            new_request_line = f"{method} {path} {parts[2]}\r\n".encode("utf-8")
            server_socket.sendall(new_request_line + headers)

            t1 = threading.Thread(target=pipe, args=(client_socket, server_socket, "up"), daemon=True)
            t2 = threading.Thread(target=pipe, args=(server_socket, client_socket, "down"), daemon=True)
            t1.start()
            t2.start()
            t1.join()
            t2.join()

    except Exception as e:
        pass
    finally:
        try:
            client_socket.close()
        except Exception:
            pass

def reporter():
    last_val = 0
    while True:
        time.sleep(5)
        with bytes_lock:
            cur = total_bytes_transferred
        diff = cur - last_val
        last_val = cur
        if cur > 0:
            speed_mb = diff / (5 * 1024 * 1024)
            total_mb = cur / (1024 * 1024)
            print(f"[PROXY STATS] Total: {total_mb:.2f} MB | Speed: {speed_mb:.2f} MB/s")
            sys.stdout.flush()

def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen(128)
    print(f"[*] USB Reverse Proxy started on {HOST}:{PORT}")
    sys.stdout.flush()

    rep_thread = threading.Thread(target=reporter, daemon=True)
    rep_thread.start()

    while True:
        client, addr = server.accept()
        t = threading.Thread(target=handle_client, args=(client, addr), daemon=True)
        t.start()

if __name__ == "__main__":
    main()
