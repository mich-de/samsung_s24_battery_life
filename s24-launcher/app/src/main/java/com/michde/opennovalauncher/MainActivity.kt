package com.michde.opennovalauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michde.opennovalauncher.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppAdapter
    private lateinit var prefs: SharedPreferences
    private var allApps: List<AppInfo> = emptyList()
    private var currentCategory: String = "ALL"

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("OpenNovaPrefs", Context.MODE_PRIVATE)

        registerPackageReceiver()
        setupWindowInsets()
        setupRecyclerView()
        setupHostAICard()
        setupSearch()
        setupCategoryTabs()
        setupSettingsBadge()
        loadApps()
    }

    private fun registerPackageReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageChangeReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageChangeReceiver)
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            val extraBottomClearance = (32 * resources.displayMetrics.density).toInt()
            val totalBottomPadding = (navBars.bottom.coerceAtLeast(systemBars.bottom)) + extraBottomClearance

            binding.appsRecyclerView.setPadding(
                binding.appsRecyclerView.paddingLeft,
                binding.appsRecyclerView.paddingTop,
                binding.appsRecyclerView.paddingRight,
                totalBottomPadding
            )
            insets
        }
    }

    private fun getGridColumns(): Int {
        return prefs.getInt("grid_columns", 4)
    }

    private fun setGridColumns(cols: Int) {
        prefs.edit().putInt("grid_columns", cols).apply()
        binding.appsRecyclerView.layoutManager = GridLayoutManager(this, cols)
    }

    private fun getHiddenPackages(): Set<String> {
        return prefs.getStringSet("hidden_packages", emptySet()) ?: emptySet()
    }

    private fun hidePackage(pkg: String) {
        val current = getHiddenPackages().toMutableSet()
        current.add(pkg)
        prefs.edit().putStringSet("hidden_packages", current).apply()
        loadApps()
    }

    private fun unhidePackage(pkg: String) {
        val current = getHiddenPackages().toMutableSet()
        current.remove(pkg)
        prefs.edit().putStringSet("hidden_packages", current).apply()
        loadApps()
    }

    // --- Folders Management (JSON persistence) ---

    private fun getFoldersJson(): JSONObject {
        val raw = prefs.getString("folders_data", "{}") ?: "{}"
        return try {
            JSONObject(raw)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun saveFoldersJson(json: JSONObject) {
        prefs.edit().putString("folders_data", json.toString()).apply()
        loadApps()
    }

    private fun addAppToFolder(folderId: String, packageName: String) {
        val json = getFoldersJson()
        if (json.has(folderId)) {
            val fObj = json.getJSONObject(folderId)
            val pkgs = fObj.getJSONArray("packages")
            var exists = false
            for (i in 0 until pkgs.length()) {
                if (pkgs.getString(i) == packageName) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                pkgs.put(packageName)
                saveFoldersJson(json)
            }
        }
    }

    private fun createFolder(name: String, initialPkg: String? = null) {
        val json = getFoldersJson()
        val folderId = "folder_" + UUID.randomUUID().toString().substring(0, 8)
        val fObj = JSONObject()
        fObj.put("name", name)
        val pkgs = JSONArray()
        if (initialPkg != null) {
            pkgs.put(initialPkg)
        }
        fObj.put("packages", pkgs)
        json.put(folderId, fObj)
        saveFoldersJson(json)
    }

    private fun removeAppFromFolder(folderId: String, packageName: String) {
        val json = getFoldersJson()
        if (json.has(folderId)) {
            val fObj = json.getJSONObject(folderId)
            val pkgs = fObj.getJSONArray("packages")
            val newPkgs = JSONArray()
            for (i in 0 until pkgs.length()) {
                val p = pkgs.getString(i)
                if (p != packageName) {
                    newPkgs.put(p)
                }
            }
            fObj.put("packages", newPkgs)
            if (newPkgs.length() == 0) {
                json.remove(folderId)
            }
            saveFoldersJson(json)
        }
    }

    private fun renameFolder(folderId: String, newName: String) {
        val json = getFoldersJson()
        if (json.has(folderId)) {
            val fObj = json.getJSONObject(folderId)
            fObj.put("name", newName)
            saveFoldersJson(json)
        }
    }

    private fun deleteFolder(folderId: String) {
        val json = getFoldersJson()
        json.remove(folderId)
        saveFoldersJson(json)
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter(
            context = this,
            onAppClick = { app ->
                launchApp(app)
            },
            onAppLongClick = { app ->
                showAppOptionsDialog(app)
            },
            onFolderClick = { folder ->
                showFolderContentsDialog(folder)
            },
            onFolderLongClick = { folder ->
                showFolderOptionsDialog(folder)
            }
        )
        binding.appsRecyclerView.layoutManager = GridLayoutManager(this, getGridColumns())
        binding.appsRecyclerView.adapter = adapter
    }

    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Impossibile avviare ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchHostAIApp() {
        val intent = packageManager.getLaunchIntentForPackage("com.wannaphong.hostai")
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            try {
                val directIntent = Intent().apply {
                    setClassName("com.wannaphong.hostai", "com.wannaphong.hostai.MainActivity")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(directIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "HostAI non e' installato", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupHostAICard() {
        binding.btnLaunchHostAI.setOnClickListener {
            launchHostAIApp()
        }

        binding.btnWebChat.setOnClickListener {
            val chatUrl = "http://127.0.0.1:8080/chat"
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(chatUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Impossibile aprire Web Chat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardHostAI.setOnClickListener {
            launchHostAIApp()
        }
    }

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSettingsBadge() {
        binding.badgePrime.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun setupCategoryTabs() {
        binding.tabAll.setOnClickListener { selectCategory("ALL") }
        binding.tabAI.setOnClickListener { selectCategory("AI") }
        binding.tabStores.setOnClickListener { selectCategory("STORES") }
        binding.tabSystem.setOnClickListener { selectCategory("SYSTEM") }
        binding.tabSettings.setOnClickListener { showSettingsDialog() }
    }

    private fun selectCategory(cat: String) {
        currentCategory = cat
        updateCategoryTabStyles()
        applyCategoryFilter()
    }

    private fun updateCategoryTabStyles() {
        fun styleTab(tv: TextView, isSelected: Boolean) {
            if (isSelected) {
                tv.setTextColor(Color.parseColor("#0E0E0E"))
                tv.setBackgroundResource(R.drawable.bg_tab_active)
            } else {
                tv.setTextColor(Color.parseColor("#9A968F"))
                tv.setBackgroundResource(R.drawable.bg_tab_inactive)
            }
        }

        styleTab(binding.tabAll, currentCategory == "ALL")
        styleTab(binding.tabAI, currentCategory == "AI")
        styleTab(binding.tabStores, currentCategory == "STORES")
        styleTab(binding.tabSystem, currentCategory == "SYSTEM")
    }

    private fun applyCategoryFilter() {
        val hidden = getHiddenPackages()
        val visibleApps = allApps.filter { !hidden.contains(it.packageName) }
        val appMap = visibleApps.associateBy { it.packageName }

        val foldersJson = getFoldersJson()
        val assignedPackages = mutableSetOf<String>()
        val folderItems = mutableListOf<LauncherItem.Folder>()

        val keys = foldersJson.keys()
        while (keys.hasNext()) {
            val fId = keys.next()
            val fObj = foldersJson.getJSONObject(fId)
            val fName = fObj.getString("name")
            val pkgs = fObj.getJSONArray("packages")
            val fApps = mutableListOf<AppInfo>()

            for (i in 0 until pkgs.length()) {
                val p = pkgs.getString(i)
                if (appMap.containsKey(p)) {
                    fApps.add(appMap[p]!!)
                    assignedPackages.add(p)
                }
            }

            if (fApps.isNotEmpty()) {
                folderItems.add(LauncherItem.Folder(fId, fName, fApps))
            }
        }

        val items = mutableListOf<LauncherItem>()

        when (currentCategory) {
            "AI" -> {
                val aiApps = visibleApps.filter {
                    val p = it.packageName.lowercase()
                    p.contains("hostai") || p.contains("dicio") || p.contains("optimizer") || p.contains("shizuku") || p.contains("ai")
                }
                items.addAll(aiApps.map { LauncherItem.SingleApp(it) })
            }
            "STORES" -> {
                val storeApps = visibleApps.filter {
                    val p = it.packageName.lowercase()
                    p.contains("aurora") || p.contains("fdroid") || p.contains("vending") || p.contains("revanced") || p.contains("browser") || p.contains("brave") || p.contains("chrome")
                }
                items.addAll(storeApps.map { LauncherItem.SingleApp(it) })
            }
            "SYSTEM" -> {
                val sysApps = visibleApps.filter {
                    val p = it.packageName.lowercase()
                    p.contains("settings") || p.contains("telecom") || p.contains("dialer") || p.contains("contact") || p.contains("camera") || p.contains("android")
                }
                items.addAll(sysApps.map { LauncherItem.SingleApp(it) })
            }
            else -> {
                // "ALL": Show Folders first, then remaining standalone apps
                items.addAll(folderItems)
                val unassignedApps = visibleApps.filter { !assignedPackages.contains(it.packageName) }
                items.addAll(unassignedApps.map { LauncherItem.SingleApp(it) })
            }
        }

        adapter.setItems(items)
        val query = binding.searchBar.text.toString()
        if (query.isNotEmpty()) {
            adapter.filter(query)
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val appList = mutableListOf<AppInfo>()

        for (info in resolveInfos) {
            val pkgName = info.activityInfo.packageName
            if (pkgName == packageName) continue

            val label = info.loadLabel(pm).toString()
            val rawIcon = info.loadIcon(pm)
            val styledIcon = TechnicalMinimalIconEngine.getStyledIcon(this, pkgName, rawIcon)
            appList.add(AppInfo(label, pkgName, styledIcon))
        }

        appList.sortBy { it.label.lowercase() }
        allApps = appList
        applyCategoryFilter()
    }

    private fun showAppOptionsDialog(app: AppInfo) {
        val options = arrayOf(
            "[ 01 ] // INFO APPLICAZIONE",
            "[ 02 ] // AGGIUNGI A CARTELLA",
            "[ 03 ] // NASCONDI DA DRAWER",
            "[ 04 ] // DISINSTALLA",
            "[ 05 ] // CONDIVIDI LINK",
            "[ 06 ] // APRI NELLO STORE"
        )
        AlertDialog.Builder(this)
            .setTitle("// APP CONFIG: ${app.label.uppercase()}")
            .setIcon(app.icon)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openAppDetails(app.packageName)
                    1 -> showAddToFolderDialog(app)
                    2 -> {
                        hidePackage(app.packageName)
                        Toast.makeText(this, "${app.label} nascosta dal drawer", Toast.LENGTH_SHORT).show()
                    }
                    3 -> uninstallApp(app)
                    4 -> shareAppLink(app)
                    5 -> openInStore(app.packageName)
                }
            }
            .show()
    }

    private fun showAddToFolderDialog(app: AppInfo) {
        val json = getFoldersJson()
        val folderList = mutableListOf<Pair<String, String>>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val fId = keys.next()
            val fName = json.getJSONObject(fId).getString("name")
            folderList.add(fId to fName)
        }

        val choices = mutableListOf<String>()
        choices.add("+ Nuova Cartella...")
        for (f in folderList) {
            choices.add("Cartella: ${f.second}")
        }

        AlertDialog.Builder(this)
            .setTitle("Aggiungi ${app.label} a Cartella")
            .setItems(choices.toTypedArray()) { _, idx ->
                if (idx == 0) {
                    // Create new folder dialog
                    val input = EditText(this).apply {
                        hint = "Nome Cartella (es. Social, AI, Tools)"
                        inputType = InputType.TYPE_CLASS_TEXT
                    }
                    AlertDialog.Builder(this)
                        .setTitle("Crea Nuova Cartella")
                        .setView(input)
                        .setPositiveButton("Crea") { _, _ ->
                            val name = input.text.toString().trim()
                            if (name.isNotEmpty()) {
                                createFolder(name, app.packageName)
                                Toast.makeText(this, "Aggiunta a cartella '$name'", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Annulla", null)
                        .show()
                } else {
                    val selectedFolderId = folderList[idx - 1].first
                    val folderName = folderList[idx - 1].second
                    addAppToFolder(selectedFolderId, app.packageName)
                    Toast.makeText(this, "Aggiunta a '$folderName'", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showFolderContentsDialog(folder: LauncherItem.Folder) {
        val appLabels = folder.apps.map { it.label }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("📁 ${folder.name}")
            .setItems(appLabels) { _, idx ->
                val clickedApp = folder.apps[idx]
                showFolderAppActionDialog(folder, clickedApp)
            }
            .setPositiveButton("Opzioni Cartella") { _, _ ->
                showFolderOptionsDialog(folder)
            }
            .setNegativeButton("Chiudi", null)
            .show()
    }

    private fun showFolderAppActionDialog(folder: LauncherItem.Folder, app: AppInfo) {
        val actions = arrayOf(
            "Apri ${app.label}",
            "Rimuovi da '${folder.name}'",
            "Disinstalla",
            "Info applicazione"
        )
        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setIcon(app.icon)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> launchApp(app)
                    1 -> {
                        removeAppFromFolder(folder.folderId, app.packageName)
                        Toast.makeText(this, "${app.label} rimossa da ${folder.name}", Toast.LENGTH_SHORT).show()
                    }
                    2 -> uninstallApp(app)
                    3 -> openAppDetails(app.packageName)
                }
            }
            .show()
    }

    private fun showRemoveAppFromFolderDialog(folder: LauncherItem.Folder) {
        val appLabels = folder.apps.map { it.label }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Tocca l'app da rimuovere da ${folder.name}")
            .setItems(appLabels) { _, idx ->
                val appToRemove = folder.apps[idx]
                removeAppFromFolder(folder.folderId, appToRemove.packageName)
                Toast.makeText(this, "${appToRemove.label} rimossa da ${folder.name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showFolderOptionsDialog(folder: LauncherItem.Folder) {
        val opts = arrayOf("Rinomina Cartella", "Elimina Cartella (le app restano nel drawer)")
        AlertDialog.Builder(this)
            .setTitle("Opzioni: ${folder.name}")
            .setItems(opts) { _, which ->
                when (which) {
                    0 -> {
                        val input = EditText(this).apply {
                            setText(folder.name)
                            selectAll()
                        }
                        AlertDialog.Builder(this)
                            .setTitle("Rinomina Cartella")
                            .setView(input)
                            .setPositiveButton("Salva") { _, _ ->
                                val newName = input.text.toString().trim()
                                if (newName.isNotEmpty()) {
                                    renameFolder(folder.folderId, newName)
                                    Toast.makeText(this, "Cartella rinominata in '$newName'", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("Annulla", null)
                            .show()
                    }
                    1 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Eliminare la cartella ${folder.name}?")
                            .setMessage("Le applicazioni contenute torneranno visibili nel drawer principale.")
                            .setPositiveButton("Elimina") { _, _ ->
                                deleteFolder(folder.folderId)
                                Toast.makeText(this, "Cartella eliminata", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Annulla", null)
                            .show()
                    }
                }
            }
            .show()
    }

    private fun openAppDetails(pkg: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$pkg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun shareAppLink(app: AppInfo) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${app.packageName}")
        }
        startActivity(Intent.createChooser(shareIntent, "Condividi ${app.label}"))
    }

    private fun openInStore(pkg: String) {
        val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(storeIntent)
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
        }
    }

    private fun uninstallApp(app: AppInfo) {
        val packageUri = Uri.fromParts("package", app.packageName, null)
        val intent = Intent(Intent.ACTION_DELETE, packageUri).apply {
            putExtra("android.intent.extra.UNINSTALL_ALL_USERS", false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri).apply {
                    putExtra("android.intent.extra.UNINSTALL_ALL_USERS", false)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(fallback)
            } catch (e2: Exception) {
                try {
                    openAppDetails(app.packageName)
                } catch (e3: Exception) {
                    Toast.makeText(this, "Impossibile disinstallare: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSettingsDialog() {
        val options = arrayOf(
            "[ 01 ] // GRIGLIA ICONE (3 / 4 / 5 COLONNE)",
            "[ 02 ] // GESTIONE APP NASCOSTE (${getHiddenPackages().size} INACTIVE)",
            "[ 03 ] // INFO & TELEMETRIA APPLIANCE"
        )

        AlertDialog.Builder(this)
            .setTitle("// LAUNCHER CONFIGURATION")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showGridSelectorDialog()
                    1 -> showHiddenAppsManagerDialog()
                    2 -> showInfoDialog()
                }
            }
            .show()
    }

    private fun showGridSelectorDialog() {
        val choices = arrayOf("[ 01 ] // 3 COLONNE (LARGE)", "[ 02 ] // 4 COLONNE (STANDARD)", "[ 03 ] // 5 COLONNE (COMPACT)")
        val currentCols = getGridColumns()
        val checkedItem = when (currentCols) {
            3 -> 0
            5 -> 2
            else -> 1
        }

        AlertDialog.Builder(this)
            .setTitle("// SELEZIONA COLONNE GRIGLIA")
            .setSingleChoiceItems(choices, checkedItem) { dialog, which ->
                val newCols = when (which) {
                    0 -> 3
                    2 -> 5
                    else -> 4
                }
                setGridColumns(newCols)
                dialog.dismiss()
            }
            .show()
    }

    private fun showHiddenAppsManagerDialog() {
        val hidden = getHiddenPackages().toList()
        if (hidden.isEmpty()) {
            Toast.makeText(this, "Nessuna app nascosta", Toast.LENGTH_SHORT).show()
            return
        }

        val hiddenNames = hidden.map { pkg ->
            try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString().uppercase()
            } catch (e: Exception) {
                pkg
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("// APP NASCOSTE // TOCCA PER RIPRISTINARE")
            .setItems(hiddenNames) { _, index ->
                val pkgToUnhide = hidden[index]
                unhidePackage(pkgToUnhide)
                Toast.makeText(this, "${hiddenNames[index]} ripristinata", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("CHIUDI", null)
            .show()
    }

    private fun showInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("// OPENNOVA S24 // TECHNICAL MINIMAL")
            .setMessage("Appliance Launcher • Teenage Engineering / Nothing Style\n\n• Industrial Dot-Matrix & Monochrome Icon Engine\n• Hairline 1px Grid Architecture & Pure OLED Black\n• Cartelle personalizzate con indicatore dot-matrix\n• Disinstallazione smart (app utente e sistema)\n• Zero telemetria & Zero background services\n• Gestures native (Swipe Down, Double Tap per HostAI)\n• HostAI Studio & Web Chat integrati\n• Ottimizzato per Galaxy S24 One UI 9 Exynos 2400")
            .setPositiveButton("CONFERMA", null)
            .show()
    }

    private val gestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                launchHostAIApp()
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && (e2.y - e1.y > 200) && Math.abs(velocityY) > 100) {
                    expandNotificationPanel()
                    return true
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    @android.annotation.SuppressLint("WrongConstant")
    private fun expandNotificationPanel() {
        try {
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusBarManager.getMethod("expandNotificationsPanel")
            expand.invoke(statusBarService)
        } catch (e: Exception) {
            // ignore
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.searchBar.text.isNotEmpty()) {
            binding.searchBar.text.clear()
        }
    }
}
