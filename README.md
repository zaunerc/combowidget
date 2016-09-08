Demo of a custom SWT combo widget (Bullet Proof Combo).

# Features

* Enhanced auto-completion / search and filtering functionalities. To
use give focus to the combo and
  * type to enable auto-completion / search.
  * or type `:` to enable substring filtering.
    * Case-insensitve.
    * `*` can be used as a wildcard.
  * or type `::` to enable RegEx filtering.
    * Case-sensitive.
  * or type `Del` to reset input.

* The Bullet Proof Combo makes sure that there is always a valid
selection present. Programmer does not ned to implement any extra
validations.

* Offers two modes (boolean allowEmptySelection): 

```Java
/**
 * @param allowEmptySelection If the combo is optional no item is pre-selected and
 *        the user is able to empty the selection.
 *
 */
public ComboWidget(Composite parent, int style, boolean allowEmptySelection) {
	<...>
}
```

# Bugs

* [Eclipse Bug 222752: READ_ONLY Combo with List visible can lose programatically-setSelection](https://bugs.eclipse.org/bugs/show_bug.cgi?id=222752)

