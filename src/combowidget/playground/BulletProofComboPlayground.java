package combowidget.playground;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import combowidget.impl.ComboWidget;

public class BulletProofComboPlayground {

    protected Shell shell;
    protected static final List<String> items = Data.ITEMS;

    private ComboWidget comboViewerBpc;
    private ComboWidget comboViewerBpcOpt;
    private ComboViewer comboViewerRoc;
    private ComboViewer comboViewerRwc;
    private ComboViewer comboViewerRocWithAssist;
    private ComboViewer comboViewerRwcWithAssist;

    private Label lblSelBpc;
    private Label lblSelBpcOpt;
    private Label lblSelRoc;
    private Label lblSelRwc;
    private Label lblSelRocWithAssist;
    private Label lblSelRwcWithAssist;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {

        try {
            BulletProofComboPlayground window = new BulletProofComboPlayground();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open the window.
     */
    public void open() {

        printDebugInfo();

        Display display = Display.getDefault();

        Realm.runWithDefault(SWTObservables.getRealm(display),new Runnable() {

            @Override
            public void run() {
                createContents();
                shell.open();
                shell.layout();
                while (!shell.isDisposed()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            }
        }
        );

    }

    private void printDebugInfo() {

        StringBuilder classPaths = new StringBuilder();

        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();
        for (URL url : urls) {
            classPaths.append(url.getFile());
            classPaths.append("\n");
        }

        StringBuilder msg = new StringBuilder();
        msg.append("System class loader class paths:\n");
        msg.append("V--- Class paths begin ---V\n");
        msg.append(classPaths);
        msg.append("A--- Class paths end   ---A");

        System.out.println(msg);
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {

        shell = new Shell();
        shell.setSize(1200, 500);
        shell.setText("SWT Application");

        GridLayout gl_shell = new GridLayout(4, false);
        gl_shell.horizontalSpacing = 20;
        shell.setLayout(gl_shell);

        Label lblWidgetClass = new Label(shell, SWT.NONE);
        lblWidgetClass.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblWidgetClass.setText("DESCRIPTION");
        lblWidgetClass.setAlignment(SWT.CENTER);

        Label lblWidget = new Label(shell, SWT.NONE);
        lblWidget.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblWidget.setAlignment(SWT.CENTER);
        lblWidget.setText("WIDGET");

        DataBindingContext ctx = new DataBindingContext();

        Label lblCurrentSelection = new Label(shell, SWT.NONE);
        lblCurrentSelection.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        lblCurrentSelection.setAlignment(SWT.CENTER);
        lblCurrentSelection.setText("CURRENT SELECTION (data binding)");

        createColumnToManuallyReadSelections();

        createBulletProofCombo(ctx);
        createBulletProofComboOpt(ctx);

        createReadOnlyCombo(ctx);
        createReadWriteCombo(ctx);

        createReadOnlyComboWithFieldAssist(ctx);
        createReadWriteComboWithFieldAssist(ctx);
    }

    protected void createColumnToManuallyReadSelections() {

        Button btn = new Button(shell, SWT.NONE);
        btn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
        btn.setText("manually read selection");

        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // BPC
                String comboText = comboViewerBpc.getControl().getText();
                String comboItem = comboViewerBpc.getControl().getSelectionIndex() < 0 ? "n/a (sel. index is < 0)" : comboViewerBpc.getControl().getItem(comboViewerBpc.getControl().getSelectionIndex());
                String structuredSelectionAsStr = ((String)comboViewerBpc.getViewer().getStructuredSelection().getFirstElement());

                StringBuilder sb = new StringBuilder();
                sb.append("combo.getText():\t\t" + comboText + "\n" );
                sb.append("combo.getItem():\t\t" + comboItem + "\n" );
                sb.append("structuredSelection:\t" + structuredSelectionAsStr);

                lblSelBpc.setText(sb.toString());

                // BPC optional
                comboText = comboViewerBpc.getControl().getText();
                comboItem = comboViewerBpc.getControl().getSelectionIndex() < 0 ? "n/a (sel. index is < 0)" : comboViewerBpc.getControl().getItem(comboViewerBpc.getControl().getSelectionIndex());
                structuredSelectionAsStr = ((String)comboViewerBpc.getViewer().getStructuredSelection().getFirstElement());

                sb = new StringBuilder();
                sb.append("combo.getText():\t\t" + comboText + "\n" );
                sb.append("combo.getItem():\t\t" + comboItem + "\n" );
                sb.append("structuredSelection:\t" + structuredSelectionAsStr);

                lblSelBpcOpt.setText(sb.toString());

                // read-only combo
                comboText = comboViewerRoc.getCombo().getText();
                comboItem = comboViewerRoc.getCombo().getSelectionIndex() < 0 ? "n/a (sel. index is < 0)" : comboViewerRoc.getCombo().getItem(comboViewerRoc.getCombo().getSelectionIndex());
                structuredSelectionAsStr = ((String)comboViewerRoc.getStructuredSelection().getFirstElement());

                sb = new StringBuilder();
                sb.append("combo.getText():\t\t" + comboText + "\n" );
                sb.append("combo.getItem():\t\t" + comboItem + "\n" );
                sb.append("structuredSelection:\t" + structuredSelectionAsStr);

                lblSelRoc.setText(sb.toString());

                // read-write combo
                comboText = comboViewerRwc.getCombo().getText();
                comboItem = comboViewerRwc.getCombo().getSelectionIndex() < 0 ? "n/a (sel. index is < 0)" : comboViewerRwc.getCombo().getItem(comboViewerRwc.getCombo().getSelectionIndex());
                structuredSelectionAsStr = ((String)comboViewerRwc.getStructuredSelection().getFirstElement());

                sb = new StringBuilder();
                sb.append("combo.getText():\t\t" + comboText + "\n" );
                sb.append("combo.getItem():\t\t" + comboItem + "\n" );
                sb.append("structuredSelection:\t" + structuredSelectionAsStr);

                lblSelRwc.setText(sb.toString());

                // read-only combo with field assist
                comboText = comboViewerRocWithAssist.getCombo().getText();
                comboItem = comboViewerRocWithAssist.getCombo().getSelectionIndex() < 0 ? "n/a (sel. index is < 0)"
                        : comboViewerRocWithAssist.getCombo()
                                .getItem(comboViewerRocWithAssist.getCombo().getSelectionIndex());
                structuredSelectionAsStr = ((String)comboViewerRocWithAssist.getStructuredSelection().getFirstElement());

                sb = new StringBuilder();
                sb.append("combo.getText():\t\t" + comboText + "\n" );
                sb.append("combo.getItem():\t\t" + comboItem + "\n" );
                sb.append("structuredSelection:\t" + structuredSelectionAsStr);

                lblSelRocWithAssist.setText(sb.toString());

                // read-write combo with field assist
                comboText = comboViewerRwcWithAssist.getCombo().getText();
                comboItem = comboViewerRwcWithAssist.getCombo().getSelectionIndex() < 0 ? "n/a (sel. index is < 0)"
                        : comboViewerRwcWithAssist.getCombo()
                                .getItem(comboViewerRwcWithAssist.getCombo().getSelectionIndex());
                structuredSelectionAsStr = ((String)comboViewerRwcWithAssist.getStructuredSelection().getFirstElement());

                sb = new StringBuilder();
                sb.append("combo.getText():\t\t" + comboText + "\n" );
                sb.append("combo.getItem():\t\t" + comboItem + "\n" );
                sb.append("structuredSelection:\t" + structuredSelectionAsStr);

                lblSelRwcWithAssist.setText(sb.toString());

                shell.layout();

                super.widgetSelected(e);
            }
        });
    }

    protected void createBulletProofCombo(DataBindingContext ctx) {

        Label widgetLabel = new Label(shell, SWT.NONE);
        widgetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        widgetLabel.setText("bullet proof combo");

        comboViewerBpc = new ComboWidget(shell, SWT.NONE, false);
        comboViewerBpc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        comboViewerBpc.getViewer().setContentProvider(new ArrayContentProvider());
        comboViewerBpc.getViewer().setInput(items);

        Label currentSelection = new Label(shell, SWT.NONE);
        currentSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        currentSelection.setText("current selection");

        lblSelBpc = new Label(shell, SWT.BORDER);
        lblSelBpc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblSelBpc.setText("selection");

        IObservableValue target = WidgetProperties.text().observe(currentSelection);
        IViewerObservableValue selectedItem = ViewerProperties.singleSelection().observe(comboViewerBpc.getViewer());
        ctx.bindValue(target, selectedItem);
    }

    protected void createBulletProofComboOpt(DataBindingContext ctx) {

        Label widgetLabel = new Label(shell, SWT.NONE);
        widgetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        widgetLabel.setText("optional bullet proof combo");

        comboViewerBpcOpt = new ComboWidget(shell, SWT.NONE, true);
        comboViewerBpcOpt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        comboViewerBpcOpt.getViewer().setContentProvider(new ArrayContentProvider());
        comboViewerBpcOpt.getViewer().setInput(items);

        Label currentSelection = new Label(shell, SWT.NONE);
        currentSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        currentSelection.setText("current selection");

        lblSelBpcOpt = new Label(shell, SWT.BORDER);
        lblSelBpcOpt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblSelBpcOpt.setText("selection");

        IObservableValue target = WidgetProperties.text().observe(currentSelection);
        IViewerObservableValue selectedItem = ViewerProperties.singleSelection().observe(comboViewerBpcOpt.getViewer());
        ctx.bindValue(target, selectedItem);
    }

    protected void createReadOnlyCombo(DataBindingContext ctx) {

        Label widgetLabel = new Label(shell, SWT.READ_ONLY);
        widgetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        widgetLabel.setText("read-only combo");

        comboViewerRoc = new ComboViewer(shell, SWT.READ_ONLY);
        Combo combo = comboViewerRoc.getCombo();
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        comboViewerRoc.setContentProvider(new ArrayContentProvider());
        comboViewerRoc.setInput(items);

        Label currentSelection = new Label(shell, SWT.NONE);
        currentSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        currentSelection.setText("current selection");

        lblSelRoc = new Label(shell, SWT.BORDER);
        lblSelRoc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblSelRoc.setText("selection");

        IObservableValue target = WidgetProperties.text().observe(currentSelection);
        IViewerObservableValue selectedItem = ViewerProperties.singleSelection().observe(comboViewerRoc);
        ctx.bindValue(target, selectedItem);
    }

    protected void createReadWriteCombo(DataBindingContext ctx) {

        Label widgetLabel = new Label(shell, SWT.NONE);
        widgetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        widgetLabel.setText("read-write combo");

        comboViewerRwc = new ComboViewer(shell, SWT.NONE);
        Combo combo = comboViewerRwc.getCombo();
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        comboViewerRwc.setContentProvider(new ArrayContentProvider());
        comboViewerRwc.setInput(items);

        Label currentSelection = new Label(shell, SWT.NONE);
        currentSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        currentSelection.setText("current selection");

        lblSelRwc = new Label(shell, SWT.BORDER);
        lblSelRwc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblSelRwc.setText("selection");

        IObservableValue target = WidgetProperties.text().observe(currentSelection);
        IViewerObservableValue selectedItem = ViewerProperties.singleSelection().observe(comboViewerRwc);
        ctx.bindValue(target, selectedItem);
    }

    protected void createReadOnlyComboWithFieldAssist(DataBindingContext ctx) {

        Label widgetLabel = new Label(shell, SWT.NONE);
        widgetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        widgetLabel.setText("read-only combo with field assist");

        comboViewerRocWithAssist = new ComboViewer(shell, SWT.READ_ONLY);
        Combo combo = comboViewerRocWithAssist.getCombo();
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        comboViewerRocWithAssist.setContentProvider(new ArrayContentProvider());
        comboViewerRocWithAssist.setInput(items);

        Label currentSelection = new Label(shell, SWT.NONE);
        currentSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        currentSelection.setText("current selection");

        lblSelRocWithAssist = new Label(shell, SWT.BORDER);
        lblSelRocWithAssist.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblSelRocWithAssist.setText("selection");

        IObservableValue target = WidgetProperties.text().observe(currentSelection);
        IViewerObservableValue selectedItem = ViewerProperties.singleSelection().observe(comboViewerRocWithAssist);
        ctx.bindValue(target, selectedItem);

        new AutoCompleteField(comboViewerRocWithAssist.getCombo(), new ComboContentAdapter(), comboViewerRocWithAssist.getCombo().getItems());
    }

    protected void createReadWriteComboWithFieldAssist(DataBindingContext ctx) {

        Label widgetLabel = new Label(shell, SWT.NONE);
        widgetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        widgetLabel.setText("read-write combo with field assist");

        comboViewerRwcWithAssist = new ComboViewer(shell, SWT.NONE);
        Combo combo = comboViewerRwcWithAssist.getCombo();
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        comboViewerRwcWithAssist.setContentProvider(new ArrayContentProvider());
        comboViewerRwcWithAssist.setInput(items);

        Label currentSelection = new Label(shell, SWT.NONE);
        currentSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        currentSelection.setText("current selection");

        lblSelRwcWithAssist = new Label(shell, SWT.BORDER);
        lblSelRwcWithAssist.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblSelRwcWithAssist.setText("selection");

        IObservableValue target = WidgetProperties.text().observe(currentSelection);
        IViewerObservableValue selectedItem = ViewerProperties.singleSelection().observe(comboViewerRwcWithAssist);
        ctx.bindValue(target, selectedItem);

        new AutoCompleteField(comboViewerRwcWithAssist.getCombo(), new ComboContentAdapter(), comboViewerRwcWithAssist.getCombo().getItems());
    }
}
