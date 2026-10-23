package com.s24optimizer.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HostAiScreen(
    italian: Boolean,
    executor: AdbExecutor,
    onLog: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var hostAiInstalled by remember { mutableStateOf(false) }
    var hostAiRunning by remember { mutableStateOf(false) }
    var freeRamMB by remember { mutableStateOf(0) }
    var isChecking by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf("") }

    fun refreshTelemetry() {
        scope.launch(Dispatchers.IO) {
            isChecking = true
            val pkgCheck = executor.execute("pm list packages | grep com.wannaphong.hostai")
            val installed = pkgCheck.stdout.contains("com.wannaphong.hostai")
            val psCheck = executor.execute("ps -A | grep hostai")
            val running = psCheck.stdout.contains("hostai")

            val memCheck = executor.execute("cat /proc/meminfo | grep MemAvailable")
            val kbMatch = Regex("""\d+""").find(memCheck.stdout)?.value?.toIntOrNull() ?: 0
            val freeMB = kbMatch / 1024

            withContext(Dispatchers.Main) {
                hostAiInstalled = installed
                hostAiRunning = running
                freeRamMB = freeMB
                isChecking = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshTelemetry()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        // ── Section Technical Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "HOSTAI.",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = 1.sp,
                    color = TextPrimary,
                )
                Text(
                    text = "05 // NEURAL APPLIANCE MODE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElectricBlue,
                    letterSpacing = 1.sp,
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (hostAiRunning) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF5A1F).copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        if (hostAiRunning) Color(0xFF00E676) else ElectricBlue,
                        RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (hostAiRunning) Color(0xFF00E676) else ElectricBlue),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (hostAiRunning) "ENGINE ONLINE" else "ENGINE STANDBY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hostAiRunning) Color(0xFF00E676) else ElectricBlue,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Hardware Architecture Card ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineDim, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceCard,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "// EXYNOS 2400 HARDWARE MATRIX",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("COMPUTE GPU", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                        Text("Xclipse 940", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("AMD RDNA3 (Vulkan)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = ElectricBlue)
                    }
                    Column {
                        Text("NPU ACCEL", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                        Text("17K MAC DUAL", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Samsung NPU Engine", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF00E676))
                    }
                    Column {
                        Text("FREE RAM", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                        Text("${freeRamMB} MB", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("LPDDR5X (8.0 GB)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Action 1: Transform S24 into Dedicated HostAI Appliance ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineDim, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceCard,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "// APPLIANCE TRANSFORM ACTIONS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (italian)
                        "Ottimizza l'S24 come server di inferenza dedicato: libera la RAM, blocca app ridondanti e avvia HostAI."
                    else
                        "Optimize S24 into a dedicated inference appliance: frees RAM, disables redundant background apps, and launches HostAI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(14.dp))

                // Button: Boost & Prepare for LLM
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            onLog("[HOSTAI] Executing S24 Appliance Boost...")
                            executor.execute("am kill-all")
                            executor.execute("cmd package compile -m speed com.wannaphong.hostai")
                            executor.execute("monkey -p com.wannaphong.hostai -c android.intent.category.LAUNCHER 1")
                            withContext(Dispatchers.Main) {
                                statusMessage = if (italian) "HostAI avviato in modalità Appliance ad alte prestazioni!" else "HostAI launched in High-Performance Appliance mode!"
                                refreshTelemetry()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color.Black,
                    ),
                ) {
                    Text(
                        text = if (italian) "[ 01 ] TRASFORMA IN APPLIANCE HOSTAI" else "[ 01 ] TRANSFORM TO HOSTAI APPLIANCE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Button: Open Web Chat (Port 8080)
                OutlinedButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://127.0.0.1:8080/chat"))
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                ) {
                    Text(
                        text = "[ 02 ] APRI WEB CHAT /chat (:8080)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Action 2: Model Management & Profiles ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OutlineDim, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceCard,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "// RECOMMENDED ON-DEVICE MODELS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(12.dp))

                ModelRowItem(
                    name = "Llama-3.2-3B-Instruct-Q4_K_M",
                    profile = "RECOMMENDED // SPEED & REASONING",
                    ramUsage = "~2.2 GB RAM • Xclipse OpenCL",
                    status = "READY",
                    onSelect = {
                        scope.launch(Dispatchers.IO) {
                            executor.execute("am start -n com.wannaphong.hostai/.MainActivity")
                        }
                    },
                )

                Spacer(Modifier.height(10.dp))

                ModelRowItem(
                    name = "Gemma-2-2B-IT-Q4_K_M",
                    profile = "LIGHTWEIGHT // MINIMAL POWER",
                    ramUsage = "~1.5 GB RAM • CPU/Vulkan",
                    status = "READY",
                    onSelect = {
                        scope.launch(Dispatchers.IO) {
                            executor.execute("am start -n com.wannaphong.hostai/.MainActivity")
                        }
                    },
                )

                Spacer(Modifier.height(10.dp))

                ModelRowItem(
                    name = "Qwen2.5-Coder-1.5B-Q4_K_M",
                    profile = "SPECIALIZED // CODING & LOGIC",
                    ramUsage = "~1.2 GB RAM • Vulkan NPU",
                    status = "READY",
                    onSelect = {
                        scope.launch(Dispatchers.IO) {
                            executor.execute("am start -n com.wannaphong.hostai/.MainActivity")
                        }
                    },
                )
            }
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue, RoundedCornerShape(6.dp)),
                shape = RoundedCornerShape(6.dp),
                color = SurfaceElevated,
            ) {
                Text(
                    text = "> $statusMessage",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElectricBlue,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ModelRowItem(
    name: String,
    profile: String,
    ramUsage: String,
    status: String,
    onSelect: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDim, RoundedCornerShape(6.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(6.dp),
        color = SurfaceElevated,
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = ElectricBlue)
                Text(name, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(ramUsage, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF00E676).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(status, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
            }
        }
    }
}
