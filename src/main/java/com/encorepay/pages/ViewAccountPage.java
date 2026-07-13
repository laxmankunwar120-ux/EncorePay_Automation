package com.encorepay.pages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.encorepay.utilities.helpers.JavaScriptHelper;
import com.encorepay.utilities.helpers.WaitHelper;

/**
 * Page Object for the "View Account" screen used during Old vs New server
 * data migration validation.
 *
 * <p>The DOM of the View Account screen is not static: it exposes an
 * unpredictable set of tabs (Basic, Transactions, Extended Loan Information,
 * Linked Accounts, History, Account, Customers, etc.) and field layouts
 * (label/value pairs, tables, grids, divs, spans).</p>
 *
 * <p>This class therefore discovers tabs and fields dynamically at runtime and
 * never assumes a fixed set of fields. All waits are explicit (via
 * {@link ActionDriver} / {@link com.encorepay.utilities.helpers.WaitHelper}).
 * No field or tab is allowed to abort the run.</p>
 */
public class ViewAccountPage extends BasePage {

    private final CollectionsPage collectionsPage;

    // ── Locators (defensive, multi-variant) ────────────────────────────────────

    private final By searchInput = By.xpath(
        "//input[@type='search']"
            + " | //input[contains(@placeholder,'Search') or contains(@placeholder,'search')]"
            + " | //input[contains(@formcontrolname,'search') or contains(@formcontrolname,'Search')]"
            + " | //input[contains(@name,'search')]"
            + " | //input[contains(@class,'search')]");

    private final By searchButton = By.xpath(
        "//button[contains(normalize-space(),'Search')]"
            + " | //button[contains(@class,'search')]"
            + " | //button[contains(@aria-label,'Search')]"
            + " | //button[contains(@class,'mat-search')]");

    private final By viewAccountLocator = By.xpath(
        "//a[contains(.,'View Account')]"
            + " | //button[contains(.,'View Account')]"
            + " | //span[contains(.,'View Account')]/ancestor::*[self::a or self::button][1]"
            + " | //a[contains(.,'View') and contains(.,'Account')]");

    /**
     * JavaScript that registers {@code window.__encExtractFields(root, excludeTabPanels)}.
     * It heuristically extracts label/value pairs from tables, definition lists,
     * Angular Material form fields and generic label-like elements. Invisible
     * subtrees (inactive tab panels, hidden elements) are skipped so only the
     * visible content is captured.
     */
    private static final String EXTRACT_DEF = String.join("\n",
        "window.__encExtractFields = function(root, excludeTabPanels) {",
        "  var norm = function(s){ return (s==null?'':(''+s)).replace(/\\s+/g,' ').trim(); };",
        "  function visible(el){",
        "    if(!el || el.nodeType!==1) return false;",
        "    var r = el.getBoundingClientRect();",
        "    if(r.width<1 || r.height<1) return false;",
        "    var st = getComputedStyle(el);",
        "    if(st.display==='none' || st.visibility==='hidden' || st.opacity==='0') return false;",
        "    return true;",
        "  }",
        "  var out=[]; var seen={};",
        "  function add(label,value){",
        "    label=norm(label); value=norm(value);",
        "    if(!label || label.length>80) return;",
        "    if(seen[label]) return;",
        "    seen[label]=true; out.push([label,value]);",
        "  }",
        "  var scanRoot = root || document;",
        "  var hidden=[];",
        "  if(excludeTabPanels){",
        "    var panels = document.querySelectorAll(\"[role='tabpanel'], [class*='tab-body' i], [class*='tab-content' i], [class*='tab-pane' i]\");",
        "    for(var i=0;i<panels.length;i++){ hidden.push(panels[i]); panels[i].style.display='none'; }",
        "  }",
        "  try {",
        "    var tables = scanRoot.querySelectorAll('table');",
        "    for(var t=0;t<tables.length;t++){",
        "      var tbl=tables[t]; if(!visible(tbl)) continue;",
        "      var rows = tbl.querySelectorAll('tr'); var header=[];",
        "      for(var r=0;r<rows.length;r++){",
        "        var ths = rows[r].querySelectorAll('th');",
        "        if(ths.length){ header=[]; for(var h=0;h<ths.length;h++){ if(visible(ths[h])) header.push(norm(ths[h].textContent)); } continue; }",
        "        var tds = rows[r].querySelectorAll('td');",
        "        if(tds.length<1 || !visible(tds[0])) continue;",
        "        if(header.length===tds.length && header.length>0){",
        "          for(var c=0;c<tds.length;c++){ if(visible(tds[c])) add(header[c], tds[c].textContent); }",
        "        } else if(tds.length>=2){",
        "          var vals=[]; for(var c2=1;c2<tds.length;c2++){ if(visible(tds[c2])) vals.push(norm(tds[c2].textContent)); }",
        "          add(norm(tds[0].textContent), vals.join(' '));",
        "        } else { add(norm(tds[0].textContent), ''); }",
        "      }",
        "    }",
        "    var dls = scanRoot.querySelectorAll('dl');",
        "    for(var d=0;d<dls.length;d++){",
        "      var dtEls=dls[d].querySelectorAll('dt'); var ddEls=dls[d].querySelectorAll('dd');",
        "      for(var i2=0;i2<dtEls.length;i2++){ if(visible(dtEls[i2])) add(dtEls[i2].textContent, ddEls[i2]?ddEls[i2].textContent:''); }",
        "    }",
        "    var ffs = scanRoot.querySelectorAll('mat-form-field, [class*=\"form-field\" i]');",
        "    for(var f=0;f<ffs.length;f++){",
        "      var ff=ffs[f]; if(!visible(ff)) continue;",
        "      var lbl = ff.querySelector('mat-label, label, [class*=\"label\" i]');",
        "      var label = lbl? norm(lbl.textContent):'';",
        "      if(!label){ var inp0=ff.querySelector('input,select,textarea'); if(inp0) label=norm(inp0.getAttribute('placeholder')||''); }",
        "      var value=''; var inp=ff.querySelector('input,select,textarea');",
        "      if(inp){",
        "        if(inp.tagName==='SELECT'){ value=norm(inp.selectedOptions&&inp.selectedOptions[0]?inp.selectedOptions[0].textContent:inp.value); }",
        "        else { value=norm(inp.value); if(!value) value=norm(inp.getAttribute('ng-reflect-value')||''); if(!value) value=norm(inp.getAttribute('value')||''); }",
        "      }",
        "      if(!value){ value=norm(ff.textContent).replace(label,''); }",
        "      if(label) add(label, value);",
        "    }",
        "    var labelExpr='label, dt, th, [class*=\"label\" i], [class*=\"field-name\" i], [class*=\"key\" i], [class*=\"caption\" i], [class*=\"form-label\" i]';",
        "    var labels = scanRoot.querySelectorAll(labelExpr);",
        "    for(var l=0;l<labels.length;l++){",
        "      var el=labels[l]; if(!visible(el)) continue;",
        "      var label=norm(el.textContent); if(!label || label.length>60) continue;",
        "      var value='';",
        "      var forId = el.getAttribute && el.getAttribute('for');",
        "      if(forId){ var ctrl=document.getElementById(forId); if(ctrl){ value=ctrl.value!=null?norm(ctrl.value):norm(ctrl.textContent); } }",
        "      if(!value){ var sib=el.nextElementSibling; if(sib&&visible(sib)) value=norm(sib.textContent); }",
        "      if(!value){ var p=el.parentElement; if(p&&p.nextElementSibling&&visible(p.nextElementSibling)) value=norm(p.nextElementSibling.textContent); }",
        "      if(value) add(label, value);",
        "    }",
        "  } finally {",
        "    for(var k=0;k<hidden.length;k++){ hidden[k].style.display=''; }",
        "  }",
        "  return out;",
        "};");

    /** JavaScript that returns the visible tab labels in document order. */
    private static final String TAB_NAMES_JS = String.join("\n",
        "(function(){",
        "  var norm=function(s){ return (s==null?'':(''+s)).replace(/\\s+/g,' ').trim(); };",
        "  function visible(el){ if(!el) return false; var r=el.getBoundingClientRect(); if(r.width<1||r.height<1) return false; var st=getComputedStyle(el); return st.display!=='none'&&st.visibility!=='hidden'; }",
        "  var sel=\"[role='tab'], .mat-tab-label, .mat-mdc-tab, a[class*='tab' i]\";",
        "  var arr=Array.from(document.querySelectorAll(sel)).filter(function(el){",
        "    if(el.getAttribute('role')==='tabpanel') return false;",
        "    var cls=(el.className||'').toString().toLowerCase();",
        "    if(cls.indexOf('panel')!==-1) return false;",
        "    var txt=norm(el.textContent);",
        "    return visible(el) && txt.length>0 && txt.length<40;",
        "  });",
        "  return arr.map(function(el){ return norm(el.textContent); });",
        "})()");

    /** JavaScript that clicks the visible tab at the given index. */
    private static final String CLICK_TAB_JS = String.join("\n",
        "(function(idx){",
        "  var norm=function(s){ return (s==null?'':(''+s)).replace(/\\s+/g,' ').trim(); };",
        "  function visible(el){ if(!el) return false; var r=el.getBoundingClientRect(); if(r.width<1||r.height<1) return false; var st=getComputedStyle(el); return st.display!=='none'&&st.visibility!=='hidden'; }",
        "  var sel=\"[role='tab'], .mat-tab-label, .mat-mdc-tab, a[class*='tab' i]\";",
        "  var arr=Array.from(document.querySelectorAll(sel)).filter(function(el){",
        "    if(el.getAttribute('role')==='tabpanel') return false;",
        "    var cls=(el.className||'').toString().toLowerCase();",
        "    if(cls.indexOf('panel')!==-1) return false;",
        "    var txt=norm(el.textContent);",
        "    return visible(el) && txt.length>0 && txt.length<40;",
        "  });",
        "  if(arr[idx]){ try{ arr[idx].scrollIntoView({block:'center'}); }catch(e){} arr[idx].click(); return true; }",
        "  return false;",
        "})(arguments[0])");

    /** JavaScript that returns the currently active tab panel element (or null). */
    private static final String ACTIVE_PANEL_JS = String.join("\n",
        "(function(){",
        "  function visible(el){ if(!el) return false; var r=el.getBoundingClientRect(); if(r.width<1||r.height<1) return false; var st=getComputedStyle(el); return st.display!=='none'&&st.visibility!=='hidden'; }",
        "  var panels = Array.from(document.querySelectorAll(\"[role='tabpanel'], [class*='tab-body' i], [class*='tab-content' i], [class*='tab-pane' i]\")).filter(visible);",
        "  return panels.length? panels[0] : null;",
        "})()");

    public ViewAccountPage(WebDriver driver) {
        super(driver);
        this.collectionsPage = new CollectionsPage(driver);
    }

    /** Reuses the existing Collections navigation to reach Collection Items. */
    public void openCollectionItems() {
        collectionsPage.openCollectionItems();
        waitForUiStable();
        action.recordVerification("Navigated to Collection Items.");
    }

    /**
     * Types the search term into the collection item search box and submits the
     * search. Never throws — if search controls are unavailable the caller is
     * expected to fall back to manual row inspection.
     */
    public void searchAccount(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            action.recordVerification("No search term supplied; relying on default collection listing.");
            return;
        }
        try {
            WebElement input = WaitHelper.waitForVisible(driver, searchInput, config.getExplicitWait());
            if (input != null) {
                action.clearAndType(input, searchTerm);
                action.captureStep("Search term entered: " + searchTerm);
            }
            WebElement button = WaitHelper.waitForClickable(driver, searchButton, config.getExplicitWait());
            if (button != null) {
                action.click(button);
            } else {
                try {
                    input.sendKeys(org.openqa.selenium.Keys.ENTER);
                } catch (Exception ignored) {
                }
            }
            action.waitForUiStable();
            action.recordVerification("Search submitted for collection item: " + searchTerm);
        } catch (Exception e) {
            action.recordVerification("Search interaction failed for '" + searchTerm + "', continuing: " + e.getMessage());
        }
    }

    /**
     * Opens the "View Account" action for the first available matching row.
     *
     * @return true if a View Account control was located and clicked
     */
    public boolean openViewAccount() {
        try {
            WebElement viewLink = WaitHelper.waitForClickable(driver, viewAccountLocator, config.getExplicitWait());
            if (viewLink == null) {
                viewLink = findViewAccountViaJs();
            }
            if (viewLink == null) {
                action.recordVerification("View Account control not found; continuing without opening.");
                return false;
            }
            action.scrollToElement(viewLink);
            action.click(viewLink);
            action.waitForUiStable();
            action.captureStep("View Account opened");
            action.recordVerification("Opened View Account.");
            return true;
        } catch (Exception e) {
            action.recordVerification("Unable to open View Account: " + e.getMessage());
            return false;
        }
    }

    private WebElement findViewAccountViaJs() {
        Object found = JavaScriptHelper.execute(driver,
            "return Array.from(document.querySelectorAll('a,button,span')).find(function(el){"
                + "  var t=(el.innerText||el.textContent||'').replace(/\\s+/g,' ').trim().toLowerCase();"
                + "  return t.indexOf('view account')!==-1 && el.offsetParent!==null;"
                + "}) || null;");
        return found instanceof WebElement ? (WebElement) found : null;
    }

    /**
     * Captures every visible field from every available tab (plus any fields
     * outside the tabbed area, prefixed with {@code General}). Field keys are
     * prefixed with the tab/scope name to avoid collisions, e.g.
     * {@code Basic_Account ID}. Duplicate raw labels across scopes are also
     * disambiguated. Never throws.
     *
     * @return map of {@code Scope_Field} -> normalized value
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> captureAllAccountFields() {
        Map<String, String> result = new LinkedHashMap<>();
        JavaScriptHelper.execute(driver, EXTRACT_DEF);

        // Fields outside the tabbed area (header / summary region).
        addScope(result, "General", extractFrom(null, true));

        List<String> tabs = discoverTabs();
        if (tabs.isEmpty()) {
            action.recordVerification("No tabs discovered; captured account fields from the visible document.");
            return result;
        }

        for (int i = 0; i < tabs.size(); i++) {
            String tabName = normalizeScopeName(tabs.get(i));
            try {
                JavaScriptHelper.execute(driver, CLICK_TAB_JS, i);
                action.waitForUiStable();
                Object panel = JavaScriptHelper.execute(driver, ACTIVE_PANEL_JS);
                WebElement panelElement = (panel instanceof WebElement) ? (WebElement) panel : null;
                List<List<String>> pairs = extractFrom(panelElement, false);
                if (pairs.isEmpty()) {
                    result.put(tabName + "_TAB NOT AVAILABLE", "");
                } else {
                    addScope(result, tabName, pairs);
                }
                action.recordVerification("Collected " + tabName + " tab (" + pairs.size() + " fields).");
            } catch (Exception e) {
                result.put(tabName + "_TAB NOT AVAILABLE", "");
                action.recordVerification("Tab '" + tabName + "' unavailable: " + e.getMessage());
            }
        }
        action.captureStep("Account data captured from all tabs");
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> discoverTabs() {
        List<String> tabs = new ArrayList<>();
        try {
            Object res = JavaScriptHelper.execute(driver, TAB_NAMES_JS);
            if (res instanceof List) {
                for (Object o : (List<Object>) res) {
                    String name = o == null ? "" : String.valueOf(o).trim();
                    if (!name.isBlank()) {
                        tabs.add(name);
                    }
                }
            }
        } catch (Exception e) {
            action.recordVerification("Tab discovery failed: " + e.getMessage());
        }
        return tabs;
    }

    @SuppressWarnings("unchecked")
    private List<List<String>> extractFrom(WebElement root, boolean excludeTabPanels) {
        List<List<String>> pairs = new ArrayList<>();
        try {
            Object res = JavaScriptHelper.execute(driver,
                "return window.__encExtractFields(arguments[0], arguments[1]);",
                root, excludeTabPanels);
            if (res instanceof List) {
                for (Object row : (List<Object>) res) {
                    if (row instanceof List) {
                        List<Object> r = (List<Object>) row;
                        String label = r.isEmpty() ? "" : String.valueOf(r.get(0));
                        String value = r.size() < 2 ? "" : String.valueOf(r.get(1));
                        List<String> pair = new ArrayList<>();
                        pair.add(label);
                        pair.add(value);
                        pairs.add(pair);
                    }
                }
            }
        } catch (Exception e) {
            action.recordVerification("Field extraction failed: " + e.getMessage());
        }
        return pairs;
    }

    private void addScope(Map<String, String> target, String scope, List<List<String>> pairs) {
        for (List<String> pair : pairs) {
            String label = normalize(pair.get(0));
            String value = normalize(pair.get(1));
            if (label.isBlank()) {
                continue;
            }
            String key = scope + "_" + label;
            int dup = 1;
            while (target.containsKey(key)) {
                key = scope + "_" + label + "_" + (dup++);
            }
            target.put(key, value);
        }
    }

    private String normalizeScopeName(String raw) {
        String name = raw.replaceAll("\\s+", " ").trim();
        return name.isBlank() ? "Tab" : name;
    }
}
