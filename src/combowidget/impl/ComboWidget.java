package combowidget.impl;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class ComboWidget extends Composite {

    private ExtendedComboViewer bpc;
    private Button deleteBtn;
    protected boolean optional;

    /**
     * @param allowEmptySelection If the combo is optional no item is pre-selected and
     *        the user is able to empty the selection.
     *
     */
    public ComboWidget(Composite parent, int style, boolean allowEmptySelection) {
        super(parent, style);

        this.optional = allowEmptySelection;

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        this.setLayout(gridLayout);

        // Combo

        bpc = new ExtendedComboViewer(this, style, allowEmptySelection);

        GridData gridDataCombo = new GridData();
        gridDataCombo.grabExcessHorizontalSpace = true;
        gridDataCombo.horizontalAlignment = SWT.FILL;
        bpc.getCombo().setLayoutData(gridDataCombo);

        // Delete button

        if (allowEmptySelection) {
            addDeleteButton();
        }

    }

    private void addDeleteButton() {
        deleteBtn = new Button(this, SWT.NONE);
        deleteBtn.setText("Delete");

        GridData gridDataDeleteBtn = new GridData();
        gridDataDeleteBtn.grabExcessHorizontalSpace = false;
        deleteBtn.setLayoutData(gridDataDeleteBtn);

        deleteBtn.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                bpc.setSelection(StructuredSelection.EMPTY);
            }
        });
    }

    public Combo getControl() {
        return bpc.getCombo();
    }

    public ComboViewer getViewer() {
        return bpc;
    }

}
