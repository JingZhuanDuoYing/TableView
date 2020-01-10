package com.nagihong.tableview.element

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class ChildRow<COLUMN: Column>(columns: List<COLUMN>): Row<COLUMN>(columns)