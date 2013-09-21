Change Log
==========

Version 0.2 *(2013-08-22)*
----------------------------

 * Changed `collectionFromCursor` into `listFromCursor` returning `List<T>` instead of `Collection<T>`.
 * Do not allow multiple @Column annotations with the same column name in scope of a single entity.
 * Added `readonly` argument for @Column annotation to mark that the field should be read from `Cursor`, but not included in `ContentValues`.
 * Added `treatNullAsDefault` argument for @Column annotation to mark that the column should not be included in `ContentValues`, so the backing database can use the default value for this column.


Version 0.1 *(2013-08-14)*
----------------------------

 * Initial release.