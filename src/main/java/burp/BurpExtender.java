package burp;

import burp.action.*;
import burp.ui.MainUI;

import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author EvilChen & 0chencc
 */

public class BurpExtender implements IBurpExtender, IHttpListener, IMessageEditorTabFactory, ITab {
    private final MainUI main = new MainUI();
    // Make stdout a public property. It is convenient for other class calls to 
    // output debugging information
    public static PrintWriter stdout;
    private IBurpExtenderCallbacks callbacks;
    private static IExtensionHelpers helpers;
    GetColorKey gck = new GetColorKey();
    UpgradeColor uc = new UpgradeColor();
    ProcessMessage pm = new ProcessMessage();

    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        BurpExtender.helpers = callbacks.getHelpers();

        String version = "2.4.6";
        callbacks.setExtensionName(String.format("Highlighter and Extractor - modified version of HaE (%s)", version));
        // Define output
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stdout.println("Highlight And Extractor - modified by @xnl_h4ck3r");
        stdout.println("Original version:");
        stdout.println("@First Author: EvilChen");
        stdout.println("@Second Author: 0chencc");
        stdout.println("@Github: https://github.com/gh0stkey/HaE");

        // UI
        SwingUtilities.invokeLater(this::initialize);

        callbacks.registerHttpListener(BurpExtender.this);
        callbacks.registerMessageEditorTabFactory(BurpExtender.this);
    }

    private void initialize() {
        callbacks.customizeUiComponent(main);
        callbacks.addSuiteTab(BurpExtender.this);
    }

    @Override
    public String getTabCaption() {
        return "H&E";
    }

    @Override
    public Component getUiComponent() {
        return main;
    }

    /**
     * Use processHttpMessage to do Highlighter
     */
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        // Only process if the request is in scope
        IRequestInfo reqinfo = BurpExtender.helpers.analyzeRequest(messageInfo.getHttpService(),
                messageInfo.getRequest());
        String requestUrl = reqinfo.getUrl().toString();

        // Determine whether it is a response，and the code is scoped
        // to：REPEATER、INTRUDER、PROXY (Respectively: toolFlag 64, 32, 4）
        if (callbacks.isInScope(reqinfo.getUrl()) && (toolFlag == 64 || toolFlag == 32 || toolFlag == 4)) {
            byte[] content;
            if (messageIsRequest) {
                content = messageInfo.getRequest();
            } else {
                content = messageInfo.getResponse();
            }

            IHttpService iHttpService = null;
            try {
                iHttpService = messageInfo.getHttpService();
            } catch (Exception ignored) {
            }
            // Get request host information 
            assert iHttpService != null;
            String host = iHttpService.getHost();

            String c = new String(content, StandardCharsets.UTF_8).intern();
            List<Map<String, String>> result = pm.processMessageByContent(helpers, content, messageIsRequest, true,
                    host);
            if (result != null && !result.isEmpty() && result.size() > 0) {
                String originalColor = messageInfo.getHighlight();
                String originalComment = messageInfo.getComment();
                List<String> colorList = new ArrayList<>();

                if (originalColor != null) {
                    colorList.add(originalColor);
                }

                colorList.add(result.get(0).get("color"));
                String color = uc.getEndColor(gck.getColorKeys(colorList));
                messageInfo.setHighlight(color);

                String addComment = String.join(" | ", result.get(1).get("comment"));
                String resComment = !Objects.equals(originalComment, "")
                        ? String.format("%s | %s", originalComment, addComment)
                        : addComment;

                messageInfo.setComment(resComment);
            }
        }
    }

    class MarkInfoTab implements IMessageEditorTab {
        private final JTabbedPane jTabbedPane = new JTabbedPane();
        private JTable jTable = new JTable();
        private final IMessageEditorController controller;
        private Map<String, String> extractRequestMap;
        private Map<String, String> extractResponseMap;
        private ArrayList<String> titleList = new ArrayList<>();

        public MarkInfoTab(IMessageEditorController controller, boolean editable) {
            this.controller = controller;
        }

        @Override
        public String getTabCaption() {
            return "MarkInfo";
        }

        @Override
        public Component getUiComponent() {
            jTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent arg0) {
                    jTable = (JTable) ((JScrollPane) jTabbedPane.getSelectedComponent()).getViewport().getView();
                }
            });
            return this.jTabbedPane;
        }

        @Override
        public boolean isEnabled(byte[] content, boolean isRequest) {
            String c = new String(content, StandardCharsets.UTF_8).intern();
            List<Map<String, String>> result = pm.processMessageByContent(helpers, content, isRequest, false, "");

            if (result != null && !result.isEmpty()) {
                Map<String, String> dataMap = result.get(0);
                if (isRequest) {
                    extractRequestMap = dataMap;
                } else {
                    extractResponseMap = dataMap;
                }
                return true;
            }
            return false;
        }

        @Override
        public byte[] getMessage() {
            return null;
        }

        @Override
        public boolean isModified() {
            return false;
        }

        /**
         * Shortcut key copy function
         */
        @Override
        public byte[] getSelectedData() {
            int[] selectRows = jTable.getSelectedRows();
            StringBuilder selectData = new StringBuilder();
            for (int row : selectRows) {
                selectData.append(jTable.getValueAt(row, 0).toString()).append("\n");
            }
            // For easy one-line copying, strip the last newline
            String revData = selectData.reverse().toString().replaceFirst("\n", "");
            StringBuilder retData = new StringBuilder(revData).reverse();
            return helpers.stringToBytes(retData.toString());
        }

        /**
         * Use setMessage used to do Extractor
         */
        @Override
        public void setMessage(byte[] content, boolean isRequest) {
            String c = new String(content, StandardCharsets.UTF_8).intern();
            if (content.length > 0) {
                if (isRequest) {
                    makeTable(extractRequestMap);
                } else {
                    makeTable(extractResponseMap);
                }
            }
        }

        /**
         * Create MarkInfo form
         */
        public void makeTable(Map<String, String> dataMap) {
            ArrayList<String> lTitleList = new ArrayList<>();
            dataMap.keySet().forEach(i -> {
                String[] extractData = dataMap.get(i).split("\n");
                Object[][] data = new Object[extractData.length][1];
                for (int x = 0; x < extractData.length; x++) {
                    data[x][0] = extractData[x];
                    // stdout.println(extractData[x]);
                }
                JScrollPane jScrollPane = new JScrollPane(new JTable(data, new Object[] { "Information" }));
                lTitleList.add(i);
                this.jTabbedPane.addTab(i, jScrollPane);
            });

            /*
             * Using removeAll will cause the MarkInfo UI to appear blank. In order to
             * improve the user experience, the remove method is used to delete;
             * Use the global ArrayList to traverse and delete Tabs to deal with the wrong
             * display of MarkInfo UI caused by the BurpSuite caching mechanism.
             */
            titleList.forEach(t -> {
                int indexOfTab = this.jTabbedPane.indexOfTab(t);
                if (indexOfTab != -1) {
                    this.jTabbedPane.removeTabAt(indexOfTab);
                }
            });

            titleList = lTitleList;
        }
    }

    @Override
    public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
        return new MarkInfoTab(controller, editable);
    }
}