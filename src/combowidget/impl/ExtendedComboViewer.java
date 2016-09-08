package combowidget.impl;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/*
 * PROGRAMMATIC SELECTIONS NOT PRESERVED PROBLEM
 *
 * If <code>keyEvent.doit = false</code> programmatically set selections
 * during a standard search are not preserved when the combo drop down
 * is collapsed. The following workarounds exist:
 *
 * - Hide and show the combo drop down each time when programmatically
 *   changing the selection. This results in "combo drop down flickering."
 *
 * - Register a combo drop down listener and programmatically set the
 *   selection again when the combo drop down is collapsed. The important
 *   thing here is that <code>keyEvent.doit</code> has to be
 *   <code>false</code>. The problem is that the combo drop down
 *   listener also fires if a new selection is set with e.g. ENTER.
 *
 * Things to keep in mind:
 *
 * - When using a default Windows R/O combo there exist many ways a
 *   user can manually expand/collapse a combo drop down: using ALT-key
 *   down, pressing F4, clicking the dropdown button located to the right
 *   of the Combo's edit box, ...
 *
 * - The only way to get reliable notifications of combo drop down
 *   collapsed events is to to use the {@link ComboDropDownListener}.
 *   There is no other way to get notified about the collapsing event of a
 *   combo box when e.g. clicking anywhere besides the widget.
 *
 * WINDOWS R/O COMBO SELECTION BEHAVIOUR:
 *
 * - ESC is pressed:
 *   No new selection is made.
 * - ENTER is pressed:
 *   The item which the mouse currently hovers over is selected.
 * - F4 is pressed:
 *   The item which the mouse currently hovers over is selected.
 * - Widget looses focus:
 *   No new selection is made.
 *
 * USE CASE EXAMPLES:
 *
 * - User wants to empty selection.
 */

/**
 * Creates a R/O combo box viewer and adds the following capabilities to it:
 *
 * <ul>
 * <li>Search as you type: case-insensitive, wildcards (*) are supported.</li>
 * <li>Substring search and filter: case-insensitive, wildcards (*) are supported.</li>
 * <li>RegEx search and filter: case-sensitive.</li>
 * </ul>
 *
 * Further notes:
 *
 * <ul>
 * <li>The viewer's comparator is set to {@link ViewerComparator} by default. The first
 * element in the list gets selected by default.</li>
 *
 * <li>A decoration which displays a tooltip about how to use this control is
 * displayed at {@code SWT.TOP | SWT.RIGHT}</li>
 * </ul>
 *
 * @author zaunerc
 */
public class ExtendedComboViewer extends ComboViewer {

    private static final boolean DRAW_BORDER_AROUND_CONTROL = false;

    private static final char ESCAPE_CHAR = ':';

    private String keySequence = "";
    private boolean allowEmptySelection;

    private final DefaultToolTip toolTip;
    private final Point toolTipLocation = new Point(-3, -24);

    /**
     * Creates a combo viewer on a newly-created <b>read-only</b> combo control
     * under the given parent.
     *
     * @see ComboViewer#ComboViewer(Composite, int)
     */
    public ExtendedComboViewer(Composite parent, int style, boolean allowEmptySelection) {

        super(parent, style | SWT.READ_ONLY | SWT.BORDER);

        this.allowEmptySelection = allowEmptySelection;

        addListeners();
        setComparator(new ViewerComparator());
        addDecoration(getControl());

        toolTip = new DefaultToolTip(getControl(), ToolTip.RECREATE, true);
        toolTip.deactivate();
    }

    private void addListeners() {
        getControl().addKeyListener(new CustomKeyAdapter());

        getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                LOGGER.trace("Widget lost focus. Resetting key sequence, combo filter and tooltip.");
                resetComboFiltersAndHideTooltip();
                keySequence = "";
            }
        });

        addSelectionChangedListener((selectionChangedEvent) -> {
            LOGGER.trace("Selection changed: New selection is >" + getStructuredSelection() + "< (index "
                    + getCombo().getSelectionIndex() + ")");
        });

        if (DRAW_BORDER_AROUND_CONTROL) {
            getControl().addPaintListener(new BorderPaintListener());
        }
    }

    /*
     * Selects the first item as soon as the input of the combo viewer is set.
     */
    @Override
    protected void inputChanged(Object input, Object oldInput) {
        if (allowEmptySelection) {
            LOGGER.trace("Viewer input changed. Not selecting element at index 0 because"
                    + " user does not want us to prevent empty selection state.");
            super.inputChanged(input, oldInput);
        } else {
            LOGGER.trace("Viewer input changed. Selecting element at index 0.");
            super.inputChanged(input, oldInput);
            setSelectedComboItem(0);
        }
    }

    private void addDecoration(Control control) {

        final ControlDecoration deco = new ControlDecoration(control, SWT.TOP | SWT.LEFT);

        // use an existing image
        Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL)
                .getImage();
        deco.setImage(image);

        // @formatter:off
        deco.setDescriptionText("Type to search...\n" + "\n- Use '" + ESCAPE_CHAR
                + "' to enable substring filtering:\n" + "    Case-insensitve.\n"
                + "    '*' can be used as a wildcard.\n" + "- Use '" + ESCAPE_CHAR + ESCAPE_CHAR
                + "' to enable RegEx filtering:\n" + "    Case-sensitive.\n"
                + "- Type 'Del' to reset input.");
        // @formatter:on

        deco.setShowOnlyOnFocus(true);
    }

    protected void regexSearchAndFilter() {

        LOGGER.trace("Executing regex search and filter function. keySequence is >" + keySequence + "<");

        // remove ESCAPE_CHAR
        String pattern = keySequence.substring(2);

        final String finalPattern = pattern;
        LOGGER.trace("regex filter pattern: >" + finalPattern + "<");

        boolean filterWithNoResults = true;

        for (Object element : getRawChildren(getInput())) {
            String elementLabel = ((LabelProvider) getLabelProvider()).getText(element);
            if (elementLabel.matches(finalPattern)) {
                filterWithNoResults = false;
                break;
            }
        }

        if (filterWithNoResults) {

            ViewerFilter[] viewerFilters = { new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    return true;
                }
            } };

            setFilters(viewerFilters);
            toolTip.setText(keySequence
                    + " (RegEx-Suche - Filter liefert KEINE Ergebnisse und wurde daher deaktiviert. Zeige alle "
                    + getRawChildren(getInput()).length + " Elemente)");
        } else {
            ViewerFilter[] viewerFilters = { new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    String elementLabel = ((LabelProvider) getLabelProvider()).getText(element);
                    if (elementLabel.matches(finalPattern)) {
                        return true;
                    }
                    return false;
                }
            } };

            setFilters(viewerFilters);
            toolTip.setText(keySequence + " (RegEx-Suche - zeige " + getFilteredChildren(getInput()).length + " von "
                    + getRawChildren(getInput()).length + " Elementen)");
        }

        toolTip.show(toolTipLocation);
        setSelectedComboItem(0);
    }

    protected void substrSearchAndFilter() {

        LOGGER.trace("Executing substring search and filter function. keySequence is >" + keySequence + "<");

        // remove ESCAPE_CHAR
        String pattern = keySequence.substring(1);
        pattern = ".*" + pattern.replaceAll("\\*", ".*") + ".*";

        final String finalPattern = pattern;

        LOGGER.trace("substring filter pattern: >" + finalPattern + "<");

        boolean filterWithNoResults = true;

        for (Object element : getRawChildren(getInput())) {
            String elementLabel = ((LabelProvider) getLabelProvider()).getText(element);
            if (elementLabel.toLowerCase().matches(finalPattern.toLowerCase())) {
                filterWithNoResults = false;
                break;
            }
        }

        if (filterWithNoResults) {

            ViewerFilter[] viewerFilters = { new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    return true;
                }
            } };

            setFilters(viewerFilters);
            toolTip.setText(keySequence
                    + " (Substringsuche - Filter liefert keine Ergebnisse und wurde daher deaktiviert. Zeige alle "
                    + getRawChildren(getInput()).length + " Elemente.)");
        } else {
            ViewerFilter[] viewerFilters = { new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element) {
                    String elementLabel = ((LabelProvider) getLabelProvider()).getText(element);
                    if (elementLabel.toLowerCase().matches(finalPattern.toLowerCase())) {
                        return true;
                    }
                    return false;
                }
            } };

            setFilters(viewerFilters);
            toolTip.setText(keySequence + " (Substringsuche - zeige " + getFilteredChildren(getInput()).length + " von "
                    + getRawChildren(getInput()).length + " Elementen)");
        }

        toolTip.show(toolTipLocation);
        setSelectedComboItem(0);
    }

    protected void standardSearch() {

        LOGGER.trace("Executing standard search function. keySequence is >" + keySequence + "<");

        toolTip.setText(keySequence + " (Element " + getCombo().getSelectionIndex() + " von "
                + getCombo().getItemCount() + " selektiert)");
        toolTip.show(toolTipLocation);

        int indexOfSelection = 0;
        for (String item : getCombo().getItems()) {
            if (item.toLowerCase().startsWith(keySequence.toLowerCase())) {
                setSelectedComboItem(indexOfSelection);
                return;
            }
            indexOfSelection++;
        }
    }

    protected void setSelectedComboItem(int indexOfSelection) {
        if (indexOfSelection < getCombo().getItemCount() && indexOfSelection > -1) {
            LOGGER.trace("Selecting item at index " + indexOfSelection);
            // Set structured selection to fire event for viewer binding.
            Object elementAt = getElementAt(indexOfSelection);
            setSelection(new StructuredSelection(elementAt));
        } else {
            LOGGER.trace("Not updating selection. Combo has " + getCombo().getItemCount()
                    + " items at the moment. Request was to select item at index " + indexOfSelection + ".");
        }
    }

    protected void resetComboFiltersAndHideTooltip() {
        getCombo().setListVisible(false);
        resetFilters();
        toolTip.setText("");
        toolTip.hide();
    }

    private final class BorderPaintListener implements PaintListener {
        @Override
        public void paintControl(PaintEvent event) {
            GC gc = event.gc;
            gc.setLineWidth(8);
            gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
            gc.drawRectangle(getControl().getShell().getClientArea().x, getControl().getShell().getClientArea().y,
                    getControl().getBounds().width, getControl().getBounds().height);
        }
    }

    private class CustomKeyAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent keyEvent) {

            LOGGER.trace("Received key event: >" + keyEvent.character + "<");
            LOGGER.trace("keySequence before processing key event is: >" + keySequence + "<");

            if (keyEvent.keyCode == SWT.DEL) {
                LOGGER.trace("User requested to delete any input and reset filter state.");
                keySequence = "";
                resetComboFiltersAndHideTooltip();
            } else if (Character.isLetterOrDigit(keyEvent.character)
                    || ",;.:-_+* ".contains(Character.toString(keyEvent.character)) || keyEvent.keyCode == SWT.BS) {
                /*
                 * keySequence whitelist characters: We WANT these chars to
                 * modify the keySequence and initiate a search / filtering
                 * operation.
                 */
                applySearchAndFilterOperation(keyEvent, (Combo) keyEvent.widget);
            }

            LOGGER.trace("keySequence after processing key event is: >" + keySequence + "<");
        }

        protected void applySearchAndFilterOperation(KeyEvent keyEvent, Combo combo) {

            if (keyEvent.keyCode == SWT.BS && !keySequence.isEmpty()) {

                LOGGER.trace("Removing last char from key sequence.");
                keySequence = keySequence.substring(0, keySequence.length() - 1);

                if (keySequence.isEmpty()) {
                    resetComboFiltersAndHideTooltip();
                    return;
                }
            } else if (keyEvent.keyCode == SWT.BS && keySequence.isEmpty()) {
                return;
            }

            /*
             * Cancel event propagation in order to prevent the ordinary Win32
             * R/O combo box search to kick-in.
             */
            keyEvent.doit = false;

            if (!(keyEvent.keyCode == SWT.BS)) {
                keySequence += Character.toString(keyEvent.character);
            }

            // regex search and filter
            if (keySequence.startsWith(String.valueOf(ESCAPE_CHAR) + String.valueOf(ESCAPE_CHAR))) {
                combo.setListVisible(true);
                regexSearchAndFilter();
                // substring search and filter
            } else if (keySequence.startsWith(String.valueOf(ESCAPE_CHAR))) {
                combo.setListVisible(true);
                substrSearchAndFilter();
            } else {
                // uncomment to enable workaround Eclipse Bug #222752
                //
            	//combo.setListVisible(false);
                //standardSearch();
                //combo.setListVisible(true);
            	
            	combo.setListVisible(true);
            	standardSearch();
            }
        }

    }

    /**
     * Fallback logger which can be used when SLF4J is not available. E.g. when
     * code is moved to jface/swt project/packages for debugging purposes.
     */
    private static class LOGGER {
        private static void trace(Object... args) {
            if (args.length > 0) {
                System.out.println("LOG MSG: " + args[0]);
            }
        }
    }

}
