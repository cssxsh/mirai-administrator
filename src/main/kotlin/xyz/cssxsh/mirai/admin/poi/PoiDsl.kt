package xyz.cssxsh.mirai.admin.poi

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*

@DslMarker
public annotation class PoiDsl

@PoiDsl
public inline fun workbook(path: String? = null, block: XSSFWorkbook.() -> Unit): XSSFWorkbook {
    return (if (path == null) XSSFWorkbook() else XSSFWorkbook(path)).apply(block)
}

@PoiDsl
public inline fun XSSFWorkbook.sheet(name: String, block: XSSFSheet.() -> Unit): XSSFSheet {
    return (getSheet(name) ?: createSheet(name)).apply(block)
}

@PoiDsl
public inline fun XSSFWorkbook.sheet(index: Int, block: XSSFSheet.() -> Unit): XSSFSheet {
    return getSheetAt(index).apply(block)
}

@PoiDsl
public inline fun XSSFSheet.row(index: Int, block: XSSFRow.() -> Unit): XSSFRow {
    return (getRow(index) ?: createRow(index)).apply(block)
}

@PoiDsl
public inline fun XSSFRow.cell(index: Int, block: XSSFCell.() -> Unit): XSSFCell {
    return (getCell(index) ?: createCell(index)).apply(block)
}

@PoiDsl
public inline fun XSSFCell.style(block: XSSFCellStyle.() -> Unit): CellStyle {
    val style = sheet.workbook.createCellStyle()
    style.cloneStyleFrom(cellStyle)
    style.apply(block)
    cellStyle = style
    return style
}

@PoiDsl
public inline fun XSSFCellStyle.font(block: XSSFFont.() -> Unit): XSSFFont {
    val font = font.apply(block)
    setFont(font)
    return font
}

@PoiDsl
public inline fun XSSFSheet.col(index: Int, block: CellStyle.() -> Unit): CellStyle {
    val style = getColumnStyle(index).apply(block)
    setDefaultColumnStyle(index, style)
    return style
}