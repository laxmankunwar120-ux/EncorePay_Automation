package com.encorepay.utilities;



import com.aventstack.extentreports.ExtentReports;

import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import com.aventstack.extentreports.reporter.configuration.Theme;



import com.encorepay.config.ConfigLoader;



public class ExtentManager {



    private static ExtentReports extent;



    public static ExtentReports getInstance() {

        if (extent == null) {

            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");



            spark.config().setTheme(Theme.STANDARD);

            

            ConfigLoader config = ConfigLoader.getInstance();

            String appName = config.getApplicationName();

            String clientName = config.getClientName();

            

            spark.config().setReportName(clientName + " Automation Report");

            spark.config().setDocumentTitle(clientName + " QA Report");

            spark.config().setTimeStampFormat("dd MMM yyyy HH:mm:ss");

            spark.config().setCss(

                ".brand-logo { display:none; }" +

                ".report-name { font-size:22px !important; font-weight:700 !important; color:#0d6efd !important; }" +

                ".test-name { font-weight:600 !important; }" +

                ".badge-primary { background:#0d6efd !important; }" +

                ".card { border-radius:12px !important; box-shadow:0 2px 12px rgba(0,0,0,0.08) !important; }" +

                ".test-content img { border-radius:8px; border:1px solid #dee2e6; margin:6px 4px; width:280px; cursor:pointer; transition:transform .2s; }" +

                ".test-content img:hover { transform:scale(1.03); }" +

                "body { background:#f8f9fa !important; }" +

                ".nav-wrapper { background:#ffffff !important; border-bottom:2px solid #0d6efd !important; }"

            );

            spark.config().setJs(

                "document.addEventListener('click', function(e) {" +

                "  if (e.target.tagName === 'IMG') {" +

                "    var overlay = document.createElement('div');" +

                "    overlay.style='position:fixed;inset:0;background:rgba(0,0,0,0.85);z-index:9999;display:flex;align-items:center;justify-content:center;cursor:zoom-out';" +

                "    var img = document.createElement('img');" +

                "    img.src = e.target.src;" +

                "    img.style='max-width:90vw;max-height:90vh;border-radius:10px;box-shadow:0 0 40px rgba(0,0,0,0.5)';" +

                "    overlay.appendChild(img);" +

                "    overlay.onclick = function(){ document.body.removeChild(overlay); };" +

                "    document.body.appendChild(overlay);" +

                "  }" +

                "});"

            );



            extent = new ExtentReports();

            extent.attachReporter(spark);

            

            extent.setSystemInfo("Project",     appName);

            extent.setSystemInfo("Client",      clientName);

            extent.setSystemInfo("Environment", config.getProperty("environment", "QA"));

            extent.setSystemInfo("Build",       config.getProperty("buildVersion", "1.0"));

            extent.setSystemInfo("Release",     config.getProperty("releaseName", ""));

            extent.setSystemInfo("Executed By", config.getProperty("executionOwner", ""));

            extent.setSystemInfo("OS",          System.getProperty("os.name"));

            extent.setSystemInfo("Browser",     config.getProperty("browser", "chrome"));

        }

        return extent;

    }

}