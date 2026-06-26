const escapeCell = value => {
  const text = value == null ? '' : String(value)
  return `"${text.replace(/"/g, '""')}"`
}

export const exportCsv = (filename, columns, rows) => {
  const header = columns.map(column => escapeCell(column.label)).join(',')
  const body = rows.map(row => columns
    .map(column => escapeCell(typeof column.value === 'function' ? column.value(row) : row[column.value]))
    .join(','))
  const content = `\ufeff${[header, ...body].join('\r\n')}`
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}
