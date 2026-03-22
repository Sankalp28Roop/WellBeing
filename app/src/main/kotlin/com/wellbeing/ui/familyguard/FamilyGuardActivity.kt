package com.wellbeing.ui.familyguard

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.wellbeing.ui.theme.WellBeingTheme
import com.wellbeing.ui.theme.WellbeingColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FamilyGuardActivity : ComponentActivity() {
    
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WellBeingTheme {
                FamilyGuardScreen(
                    onBack = { finish() }
                )
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun FamilyGuardScreen(onBack: () -> Unit) {
        var isLoading by remember { mutableStateOf(true) }
        var webViewError by remember { mutableStateOf<String?>(null) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FamilyGuard", color = WellbeingColors.Slate800) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = WellbeingColors.Slate500)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webView = this
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = false
                                displayZoomControls = false
                                setSupportZoom(false)
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                allowFileAccess = true
                            }
                            
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    webViewError = null
                                }
                                
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }
                                
                                override fun onReceivedError(
                                    view: WebView?,
                                    errorCode: Int,
                                    description: String?,
                                    failingUrl: String?
                                ) {
                                    webViewError = "Error: $description"
                                }
                                
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    return false
                                }
                            }
                            
                            webChromeClient = object : WebChromeClient() {}
                            
                            loadDataWithBaseURL(
                                "https://localhost",
                                getFamilyGuardHTML(),
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    },
                    update = { _ -> },
                    modifier = Modifier.fillMaxSize()
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WellbeingColors.Blue500)
                    }
                }
                
                webViewError?.let { error ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error,
                            color = WellbeingColors.Red500,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    
    private fun getFamilyGuardHTML(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>FamilyGuard</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        html, body { height: 100%; width: 100%; overflow: hidden; background: #f0f4ff; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #1e293b; }
        body.dark { background: #0f172a; color: #e2e8f0; }
        
        .app-container { height: 100%; display: flex; flex-direction: column; }
        .topbar { height: 56px; background: #fff; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; padding: 0 16px; gap: 12px; flex-shrink: 0; }
        body.dark .topbar { background: #1e293b; border-color: #334155; }
        
        .flex-1 { flex: 1; }
        .min-w-0 { min-width: 0; }
        .truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
        .font-bold { font-weight: 700; }
        .text-base { font-size: 16px; }
        
        .card { background: #fff; border-radius: 16px; box-shadow: 0 2px 12px rgba(0,0,0,.06); padding: 20px; margin-bottom: 16px; }
        body.dark .card { background: #1e293b; box-shadow: 0 2px 12px rgba(0,0,0,.3); }
        
        .grid { display: flex; flex-wrap: wrap; }
        .grid-cols-2 > * { width: 50%; padding: 8px; }
        .grid-cols-3 > * { width: 33.33%; padding: 8px; }
        .gap-4 { gap: 16px; }
        .gap-3 { gap: 12px; }
        
        .flex { display: flex; }
        .items-start { align-items: flex-start; }
        .items-center { align-items: center; }
        .justify-between { justify-content: space-between; }
        .justify-center { justify-content: center; }
        
        .text-muted { color: #64748b; }
        body.dark .text-muted { color: #94a3b8; }
        .text-heading { color: #1e293b; }
        body.dark .text-heading { color: #f1f5f9; }
        
        .text-xl { font-size: 20px; }
        .mb-1 { margin-bottom: 4px; }
        .mb-3 { margin-bottom: 12px; }
        .mb-4 { margin-bottom: 16px; }
        .mb-6 { margin-bottom: 24px; }
        .mt-3 { margin-top: 12px; }
        
        .stat-badge { display: inline-flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 700; padding: 3px 8px; border-radius: 20px; }
        .stat-badge.green { background: #dcfce7; color: #15803d; }
        .stat-badge.red { background: #fee2e2; color: #dc2626; }
        
        .toggle-wrap { position: relative; display: inline-block; width: 44px; height: 24px; }
        .toggle-wrap input { opacity: 0; width: 0; height: 0; position: absolute; }
        .toggle-slider { position: absolute; inset: 0; background: #cbd5e1; border-radius: 24px; cursor: pointer; transition: .3s; }
        .toggle-slider::before { content: ''; position: absolute; height: 18px; width: 18px; left: 3px; bottom: 3px; background: #fff; border-radius: 50%; transition: .3s; }
        .toggle-wrap input:checked + .toggle-slider { background: linear-gradient(135deg, #3b82f6, #6366f1); }
        .toggle-wrap input:checked + .toggle-slider::before { transform: translateX(20px); }
        
        .bar-track { background: #f1f5f9; border-radius: 99px; overflow: hidden; height: 8px; }
        body.dark .bar-track { background: #334155; }
        .bar-fill { height: 8px; border-radius: 99px; transition: width .8s; background: #3b82f6; }
        
        .main-scroll { flex: 1; overflow-y: auto; overflow-x: hidden; background: #f8fafc; padding: 16px; padding-bottom: 80px; }
        body.dark .main-scroll { background: #0f172a; }
        
        .section { display: none; }
        .section.active { display: block; }
        
        .chip { display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 99px; font-size: 12px; font-weight: 700; cursor: pointer; background: #f1f5f9; color: #64748b; margin-right: 8px; margin-bottom: 8px; }
        .chip.active { background: #dbeafe; color: #1d4ed8; }
        
        .bottom-tab-bar { position: fixed; bottom: 0; left: 0; right: 0; height: 64px; background: #fff; border-top: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-around; z-index: 40; }
        body.dark .bottom-tab-bar { background: #1e293b; border-color: #334155; }
        
        .tab-item { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; padding: 8px 0; cursor: pointer; font-weight: 700; font-size: 11px; color: #64748b; flex: 1; height: 100%; transition: all .18s; }
        .tab-item:hover { color: #3b82f6; }
        .tab-item.active { color: #3b82f6; }
        
        .qa-btn { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 16px; border-radius: 12px; cursor: pointer; border: none; width: 100%; }
        
        .rounded-xl { border-radius: 12px; }
        .p-3 { padding: 12px; }
        .py-3 { padding-top: 12px; padding-bottom: 12px; }
        .border-b { border-bottom: 1px solid #e2e8f0; }
        
        .focus-circle { width: 160px; height: 160px; position: relative; margin: 0 auto; }
        .focus-ring-bg { fill: none; stroke: #e2e8f0; }
        body.dark .focus-ring-bg { stroke: #334155; }
        .focus-ring-progress { fill: none; stroke-linecap: round; transition: stroke-dashoffset 1s; }
        
        .px-6 { padding-left: 24px; padding-right: 24px; }
        .py-3 { padding-top: 12px; padding-bottom: 12px; }
        .text-white { color: #fff; }
        .text-center { text-align: center; }
        
        button { font-family: inherit; }
    </style>
</head>
<body>
<div class="app-container">
    <div class="topbar">
        <div class="flex-1 min-w-0">
            <div class="text-heading font-bold text-base truncate" id="page-title">Dashboard</div>
            <div class="text-muted truncate" style="font-size: 11px;" id="page-sub">Daily overview</div>
        </div>
    </div>
    
    <div class="main-scroll">
        <div id="sec-dashboard" class="section active">
            <div class="grid grid-cols-2 gap-4">
                <div class="card">
                    <div class="flex items-start justify-between mb-3">
                        <div style="width: 40px; height: 40px; background: #dbeafe; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 18px;">⏱️</div>
                        <span class="stat-badge red">+12%</span>
                    </div>
                    <div class="text-heading" style="font-size: 26px; font-weight: 800;" id="screen-time-value">0m</div>
                    <div class="text-muted" style="font-size: 13px; font-weight: 600;">Screen Time Today</div>
                </div>
                <div class="card">
                    <div class="flex items-start justify-between mb-3">
                        <div style="width: 40px; height: 40px; background: #dcfce7; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 18px;">❤️</div>
                        <span class="stat-badge green">Good</span>
                    </div>
                    <div class="text-heading" style="font-size: 26px; font-weight: 800;" id="wellbeing-score">0</div>
                    <div class="text-muted" style="font-size: 13px; font-weight: 600;">Wellbeing Score</div>
                </div>
                <div class="card">
                    <div class="flex items-start justify-between mb-3">
                        <div style="width: 40px; height: 40px; background: #fef3c7; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 18px;">📱</div>
                    </div>
                    <div class="text-heading" style="font-size: 26px; font-weight: 800;" id="app-count">0</div>
                    <div class="text-muted" style="font-size: 13px; font-weight: 600;">Apps Used</div>
                </div>
                <div class="card">
                    <div class="flex items-start justify-between mb-3">
                        <div style="width: 40px; height: 40px; background: #f3e8ff; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 18px;">🔓</div>
                    </div>
                    <div class="text-heading" style="font-size: 26px; font-weight: 800;" id="unlock-count">0</div>
                    <div class="text-muted" style="font-size: 13px; font-weight: 600;">Unlocks Today</div>
                </div>
            </div>
            
            <div class="card">
                <div class="flex items-center justify-between mb-4">
                    <div class="text-heading font-bold" style="font-size: 15px;">App Usage Breakdown</div>
                    <span class="chip active">Today</span>
                </div>
                <div id="app-bars"></div>
            </div>
            
            <div class="card">
                <div class="text-heading font-bold mb-4" style="font-size: 15px;">Quick Actions</div>
                <div class="grid grid-cols-2 gap-3">
                    <button class="qa-btn" style="background: #fee2e2; color: #dc2626;" onclick="toggleAction('pause')">
                        <span style="font-size: 24px;">⏸️</span>
                        <span>Pause</span>
                    </button>
                    <button class="qa-btn" style="background: #fef3c7; color: #b45309;" onclick="toggleAction('lock')">
                        <span style="font-size: 24px;">🔒</span>
                        <span>Lock</span>
                    </button>
                    <button class="qa-btn" style="background: #dbeafe; color: #1d4ed8;" onclick="navigateTo('focus')">
                        <span style="font-size: 24px;">🎯</span>
                        <span>Focus</span>
                    </button>
                    <button class="qa-btn" style="background: #f3e8ff; color: #7c3aed;" onclick="navigateTo('reports')">
                        <span style="font-size: 24px;">📊</span>
                        <span>Reports</span>
                    </button>
                </div>
            </div>
        </div>
        
        <div id="sec-apps" class="section">
            <div class="text-heading font-bold text-xl mb-1">App Management</div>
            <div class="text-muted mb-4" style="font-size: 13px;">Control app access and time limits</div>
            
            <div class="flex mb-4" style="flex-wrap: wrap;">
                <span class="chip active" onclick="filterApps('all', this)">All</span>
                <span class="chip" onclick="filterApps('social', this)">📱 Social</span>
                <span class="chip" onclick="filterApps('games', this)">🎮 Games</span>
            </div>
            
            <div class="card" id="app-list"></div>
        </div>
        
        <div id="sec-focus" class="section">
            <div class="text-heading font-bold text-xl mb-1">Focus Mode</div>
            <div class="text-muted mb-6" style="font-size: 13px;">Pomodoro-style focus sessions</div>
            
            <div class="card text-center" style="padding: 32px;">
                <div class="focus-circle mb-4">
                    <svg width="160" height="160">
                        <circle class="focus-ring-bg" cx="80" cy="80" r="68" stroke-width="10"/>
                        <circle class="focus-ring-progress" cx="80" cy="80" r="68" stroke-width="10" stroke="url(#greenGrad)" id="focus-ring" stroke-dasharray="427" stroke-dashoffset="0"/>
                        <defs>
                            <linearGradient id="greenGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                                <stop offset="0%" stop-color="#22c55e"/>
                                <stop offset="100%" stop-color="#3b82f6"/>
                            </linearGradient>
                        </defs>
                    </svg>
                    <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); margin-top: -40px;">
                        <div class="text-heading" style="font-size: 36px; font-weight: 900;" id="focus-time">25:00</div>
                        <div class="text-muted" style="font-size: 12px;" id="focus-label">Ready to focus</div>
                    </div>
                </div>
                <div class="flex items-center justify-center gap-3">
                    <button id="focus-start-btn" onclick="toggleFocus()" class="px-6 py-3 rounded-xl text-white font-bold" style="background: linear-gradient(135deg, #22c55e, #16a34a); border: none; cursor: pointer; font-size: 14px;">▶ Start Focus</button>
                    <button onclick="resetFocus()" class="px-6 py-3 rounded-xl font-bold" style="background: transparent; cursor: pointer; font-size: 14px; border: 1px solid #e2e8f0; color: #1e293b;">↺ Reset</button>
                </div>
                <div class="flex items-center justify-center gap-3 mt-3">
                    <span class="chip active" onclick="setFocusTime(25, this)">25 min</span>
                    <span class="chip" onclick="setFocusTime(45, this)">45 min</span>
                    <span class="chip" onclick="setFocusTime(60, this)">60 min</span>
                </div>
            </div>
        </div>
        
        <div id="sec-reports" class="section">
            <div class="text-heading font-bold text-xl mb-1">Reports & Insights</div>
            <div class="text-muted mb-4" style="font-size: 13px;">Weekly trends</div>
            
            <div class="card">
                <div class="text-heading font-bold mb-4" style="font-size: 15px;">Screen Time Trend</div>
                <div id="trend-chart" style="display: flex; align-items: flex-end; gap: 8px; height: 120px;"></div>
                <div class="flex justify-between text-muted mt-3" style="font-size: 11px;">
                    <span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span>
                </div>
            </div>
            
            <div class="grid grid-cols-3 gap-4">
                <div class="card text-center">
                    <div class="text-heading font-bold mb-1" style="font-size: 14px;">📊 Avg Daily</div>
                    <div class="text-heading" style="font-size: 28px; font-weight: 900;" id="avg-daily">0h</div>
                </div>
                <div class="card text-center">
                    <div class="text-heading font-bold mb-1" style="font-size: 14px;">🎯 Goals Met</div>
                    <div class="text-heading" style="font-size: 28px; font-weight: 900;">0/7</div>
                </div>
                <div class="card text-center">
                    <div class="text-heading font-bold mb-1" style="font-size: 14px;">🏆 Points</div>
                    <div class="text-heading" style="font-size: 28px; font-weight: 900;">0</div>
                </div>
            </div>
        </div>
        
        <div id="sec-settings" class="section">
            <div class="text-heading font-bold text-xl mb-1">Settings</div>
            <div class="text-muted mb-6" style="font-size: 13px;">Manage preferences</div>
            
            <div class="card">
                <div class="flex items-center justify-between mb-3 p-3 rounded-xl" style="background: #f8fafc;">
                    <div>
                        <div class="text-heading font-bold" style="font-size: 14px;">Dark Mode</div>
                        <div class="text-muted" style="font-size: 12px;">Toggle dark theme</div>
                    </div>
                    <label class="toggle-wrap"><input type="checkbox" id="dark-toggle" onchange="toggleDarkMode()"><span class="toggle-slider"></span></label>
                </div>
                <div class="flex items-center justify-between p-3 rounded-xl" style="background: #f8fafc;">
                    <div class="text-heading font-bold" style="font-size: 14px;">Version</div>
                    <div class="text-muted" style="font-size: 14px;">1.0.0</div>
                </div>
            </div>
        </div>
    </div>
    
    <div class="bottom-tab-bar">
        <div class="tab-item active" onclick="navigateTo('dashboard')">📊<span>Home</span></div>
        <div class="tab-item" onclick="navigateTo('apps')">📱<span>Apps</span></div>
        <div class="tab-item" onclick="navigateTo('focus')">🎯<span>Focus</span></div>
        <div class="tab-item" onclick="navigateTo('reports')">📈<span>Reports</span></div>
        <div class="tab-item" onclick="navigateTo('settings')">⚙️<span>Settings</span></div>
    </div>
</div>

<script>
var currentSection = 'dashboard';
var darkMode = false;
var focusRunning = false;
var focusInterval = null;
var focusTotal = 25 * 60;
var focusRemaining = 25 * 60;
var appFilter = 'all';

var sampleApps = [
    { name: 'YouTube', emoji: '▶️', cat: 'entertainment', time: '1h 20m', blocked: false },
    { name: 'TikTok', emoji: '🎵', cat: 'social', time: '45m', blocked: true },
    { name: 'Instagram', emoji: '📸', cat: 'social', time: '30m', blocked: false },
    { name: 'Minecraft', emoji: '⛏️', cat: 'games', time: '55m', blocked: false },
    { name: 'Chrome', emoji: '🌐', cat: 'utility', time: '20m', blocked: false }
];

function navigateTo(section) {
    var sections = document.querySelectorAll('.section');
    for (var i = 0; i < sections.length; i++) {
        sections[i].classList.remove('active');
    }
    var targetSection = document.getElementById('sec-' + section);
    if (targetSection) {
        targetSection.classList.add('active');
    }
    
    var tabItems = document.querySelectorAll('.tab-item');
    for (var i = 0; i < tabItems.length; i++) {
        tabItems[i].classList.remove('active');
        if (tabItems[i].getAttribute('onclick') && tabItems[i].getAttribute('onclick').indexOf(section) > -1) {
            tabItems[i].classList.add('active');
        }
    }
    
    var titles = {
        dashboard: ['Dashboard', 'Daily overview'],
        apps: ['Apps', 'Manage access'],
        focus: ['Focus Mode', 'Stay focused'],
        reports: ['Reports', 'Weekly trends'],
        settings: ['Settings', 'Preferences']
    };
    
    var pageTitle = document.getElementById('page-title');
    var pageSub = document.getElementById('page-sub');
    if (titles[section]) {
        pageTitle.textContent = titles[section][0];
        pageSub.textContent = titles[section][1];
    }
    
    currentSection = section;
}

function renderAppBars() {
    var container = document.getElementById('app-bars');
    if (!container) return;
    var html = '';
    for (var i = 0; i < Math.min(sampleApps.length, 5); i++) {
        var app = sampleApps[i];
        var percent = Math.floor(Math.random() * 60) + 20;
        html += '<div class="flex items-center gap-3 mb-3">' +
            '<span style="font-size: 14px;">' + app.emoji + '</span>' +
            '<div class="flex-1"><div class="text-muted" style="font-size: 12px; margin-bottom: 4px;">' + app.name + '</div>' +
            '<div class="bar-track"><div class="bar-fill" style="width: ' + percent + '%;"></div></div></div>' +
            '<span class="text-muted" style="font-size: 12px;">' + percent + '%</span></div>';
    }
    container.innerHTML = html;
}

function renderAppList() {
    var container = document.getElementById('app-list');
    if (!container) return;
    var filtered = appFilter === 'all' ? sampleApps : sampleApps.filter(function(a) { return a.cat === appFilter; });
    var html = '';
    for (var i = 0; i < filtered.length; i++) {
        var app = filtered[i];
        html += '<div class="flex items-center gap-3 py-3 border-b">' +
        '<span style="font-size: 24px;">' + app.emoji + '</span>' +
        '<div class="flex-1">' +
        '<div class="text-heading font-bold" style="font-size: 14px;">' + app.name + '</div>' +
        '<div class="text-muted" style="font-size: 12px;">' + app.time + '</div></div>' +
        '<label class="toggle-wrap"><input type="checkbox"' + (app.blocked ? '' : ' checked') + '><span class="toggle-slider"></span></label></div>';
    }
    container.innerHTML = html;
}

function filterApps(cat, el) {
    appFilter = cat;
    var chips = document.querySelectorAll('.chip');
    for (var i = 0; i < chips.length; i++) {
        chips[i].classList.remove('active');
    }
    if (el) el.classList.add('active');
    renderAppList();
}

function renderTrendChart() {
    var container = document.getElementById('trend-chart');
    if (!container) return;
    var data = [2.5, 3.1, 4.2, 3.8, 3.2, 4.5, 3.7];
    var max = Math.max.apply(null, data);
    var html = '';
    for (var i = 0; i < data.length; i++) {
        var h = Math.round((data[i] / max) * 100);
        var color = data[i] > 4 ? '#ef4444' : data[i] > 3 ? '#f59e0b' : '#22c55e';
        html += '<div style="flex: 1; height: ' + h + '%; background: ' + color + '; border-radius: 6px 6px 0 0;"></div>';
    }
    container.innerHTML = html;
}

function toggleDarkMode() {
    darkMode = !darkMode;
    document.body.classList.toggle('dark', darkMode);
}

function setFocusTime(min, el) {
    if (focusRunning) return;
    focusTotal = min * 60;
    focusRemaining = min * 60;
    var chips = document.querySelectorAll('#sec-focus .chip');
    for (var i = 0; i < chips.length; i++) {
        chips[i].classList.remove('active');
    }
    if (el) el.classList.add('active');
    updateFocusDisplay();
}

function toggleFocus() {
    var btn = document.getElementById('focus-start-btn');
    var label = document.getElementById('focus-label');
    
    if (focusRunning) {
        clearInterval(focusInterval);
        focusRunning = false;
        btn.textContent = '▶ Start Focus';
        btn.style.background = 'linear-gradient(135deg, #22c55e, #16a34a)';
        label.textContent = 'Paused';
    } else {
        focusRunning = true;
        btn.textContent = '⏸ Pause';
        btn.style.background = 'linear-gradient(135deg, #f59e0b, #ef4444)';
        label.textContent = 'Focusing...';
        focusInterval = setInterval(function() {
            focusRemaining--;
            updateFocusDisplay();
            if (focusRemaining <= 0) {
                clearInterval(focusInterval);
                focusRunning = false;
                btn.textContent = '▶ Start Focus';
                btn.style.background = 'linear-gradient(135deg, #22c55e, #16a34a)';
                label.textContent = 'Complete! 🎉';
            }
        }, 1000);
    }
}

function resetFocus() {
    clearInterval(focusInterval);
    focusRunning = false;
    focusRemaining = focusTotal;
    var btn = document.getElementById('focus-start-btn');
    var label = document.getElementById('focus-label');
    btn.textContent = '▶ Start Focus';
    btn.style.background = 'linear-gradient(135deg, #22c55e, #16a34a)';
    label.textContent = 'Ready to focus';
    updateFocusDisplay();
}

function updateFocusDisplay() {
    var m = Math.floor(focusRemaining / 60).toString().padStart(2, '0');
    var s = (focusRemaining % 60).toString().padStart(2, '0');
    var focusTime = document.getElementById('focus-time');
    var focusRing = document.getElementById('focus-ring');
    if (focusTime) focusTime.textContent = m + ':' + s;
    if (focusRing) focusRing.setAttribute('stroke-dashoffset', 427 * (focusRemaining / focusTotal));
}

function toggleAction(action) {
    console.log('Action:', action);
    if (action === 'pause') {
        alert('Pause feature coming soon!');
    } else if (action === 'lock') {
        alert('Lock feature coming soon!');
    }
}

function updateDashboard(data) {
    if (data.screenTime !== undefined) {
        var hours = Math.floor(data.screenTime / 60);
        var mins = data.screenTime % 60;
        var screenTimeValue = document.getElementById('screen-time-value');
        if (screenTimeValue) screenTimeValue.textContent = hours > 0 ? hours + 'h ' + mins + 'm' : mins + 'm';
    }
    if (data.unlocks !== undefined) {
        var unlockCount = document.getElementById('unlock-count');
        if (unlockCount) unlockCount.textContent = data.unlocks;
    }
    if (data.appCount !== undefined) {
        var appCount = document.getElementById('app-count');
        if (appCount) appCount.textContent = data.appCount;
    }
    if (data.score !== undefined) {
        var wellbeingScore = document.getElementById('wellbeing-score');
        if (wellbeingScore) wellbeingScore.textContent = data.score;
    }
}

window.updateDashboard = updateDashboard;

renderAppBars();
renderAppList();
renderTrendChart();
</script>
</body>
</html>
        """.trim()
    }
}
