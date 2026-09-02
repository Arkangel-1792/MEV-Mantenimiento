import {
  COLLECTIONS,
  calculateSummary,
  formatDate,
  getTreadValues,
  readingLabel,
  recordLabel
} from './core.js';

function xmlEscape(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&apos;'
  })[character]);
}

function htmlEscape(value) {
  return String(value ?? '').replace(/[&<>'"]/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;'
  })[character]);
}

function fileStamp() {
  const date = new Date();
  return `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`;
}

function downloadBlob(content, mimeType, fileName) {
  const blob = content instanceof Blob ? content : new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = fileName;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

function excelCell(value, style = '') {
  const isNumber = typeof value === 'number' && Number.isFinite(value);
  const type = isNumber ? 'Number' : 'String';
  return `<Cell${style ? ` ss:StyleID="${style}"` : ''}><Data ss:Type="${type}">${xmlEscape(value ?? '')}</Data></Cell>`;
}

function excelSheet(name, headers, rows) {
  const header = `<Row>${headers.map(value => excelCell(value, 'Header')).join('')}</Row>`;
  const body = rows.map(row => `<Row>${row.map(value => excelCell(value)).join('')}</Row>`).join('');
  return `<Worksheet ss:Name="${xmlEscape(name.slice(0, 31))}"><Table>${header}${body}</Table><WorksheetOptions xmlns="urn:schemas-microsoft-com:office:excel"><FreezePanes/><FrozenNoSplit/><SplitHorizontal>1</SplitHorizontal><TopRowBottomPane>1</TopRowBottomPane><ActivePane>2</ActivePane><ProtectObjects>False</ProtectObjects><ProtectScenarios>False</ProtectScenarios></WorksheetOptions></Worksheet>`;
}

function maintenanceRows(records) {
  return records.filter(record => record._collection === COLLECTIONS.maintenance).map(record => [
    record.codigoActivo, record.tipoServicio, record.kilometraje ?? '', record.horometro ?? '',
    record.accionEjecutada, record.observaciones, record.ordenTrabajo, record.numeroPedido,
    record.estadoRegistro, record.motivoDevolucion || '', record.nombreTecnico || record.emailUsuario || '',
    formatDate(record.fechaCreacion), formatDate(record.fechaActualizacion)
  ]);
}

function treadRows(records) {
  return records.filter(record => record._collection === COLLECTIONS.tread).map(record => {
    const treads = getTreadValues(record);
    return [
      record.codigoActivo, record.proyecto, record.kilometraje ?? '', record.horometro ?? '',
      ...Array.from({ length: 12 }, (_, index) => treads[index] ?? ''),
      record.estadoGeneral, record.novedad, record.nombreTecnico, record.estadoRegistro,
      record.motivoDevolucion || '', formatDate(record.fechaCreacion), formatDate(record.fechaActualizacion)
    ];
  });
}

function tireRows(records) {
  return records.filter(record => record._collection === COLLECTIONS.tire).map(record => [
    record.codigoActivo, record.proyecto, record.kilometraje ?? '', record.horometro ?? '',
    record.tipoIntervencion, record.posicion, record.huella ?? '', record.marcaLlanta,
    record.medidaLlanta, record.serieLlanta, record.motivo, record.observaciones,
    record.nombreTecnico, record.estadoRegistro, record.motivoDevolucion || '',
    formatDate(record.fechaCreacion), formatDate(record.fechaActualizacion)
  ]);
}

export function downloadExcel(records) {
  const summary = calculateSummary(records);
  const summaryRows = [
    ['Registros totales', summary.total], ['Mantenimientos', summary.totalMantenimientos],
    ['Preventivos', summary.preventivos], ['Correctivos', summary.correctivos],
    ['Tomas de huella', summary.totalHuellas], ['Mediciones críticas (≤ 6 mm)', summary.huellasCriticas],
    ['Intervenciones', summary.totalIntervenciones], ['Cambios', summary.cambios],
    ['Rotaciones', summary.rotaciones], ['Reparaciones', summary.reparaciones],
    ['Montajes', summary.montajes], ['Desmontajes', summary.desmontajes], ['Bajas', summary.bajas],
    ['Borradores', summary.borradores], ['Pendientes de revisión', summary.enviados],
    ['Aprobados', summary.aprobados], ['Devueltos', summary.devueltos]
  ];
  const workbook = `<?xml version="1.0"?><?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
<DocumentProperties xmlns="urn:schemas-microsoft-com:office:office"><Author>MEV Mantenimiento</Author><Title>Reporte consolidado MEV</Title><Created>${new Date().toISOString()}</Created></DocumentProperties>
<Styles><Style ss:ID="Default" ss:Name="Normal"><Alignment ss:Vertical="Bottom"/><Borders/><Font/><Interior/><NumberFormat/><Protection/></Style><Style ss:ID="Header"><Font ss:Bold="1" ss:Color="#FFFFFF"/><Interior ss:Color="#0F4C5C" ss:Pattern="Solid"/><Alignment ss:WrapText="1"/></Style></Styles>
${excelSheet('Resumen', ['Indicador', 'Valor'], summaryRows)}
${excelSheet('Mantenimientos', ['Activo', 'Servicio', 'Kilometraje', 'Horómetro', 'Acción ejecutada', 'Observaciones', 'Orden de trabajo', 'Pedido', 'Estado', 'Motivo devolución', 'Técnico/usuario', 'Fecha creación', 'Última actualización'], maintenanceRows(records))}
${excelSheet('Tomas de huella', ['Activo', 'Proyecto', 'Kilometraje', 'Horómetro', ...Array.from({ length: 12 }, (_, index) => `P${index + 1}`), 'Estado general', 'Novedad', 'Técnico', 'Estado', 'Motivo devolución', 'Fecha creación', 'Última actualización'], treadRows(records))}
${excelSheet('Intervenciones', ['Activo', 'Proyecto', 'Kilometraje', 'Horómetro', 'Intervención', 'Posición', 'Huella', 'Marca', 'Medida', 'Serie', 'Motivo', 'Observaciones', 'Técnico', 'Estado', 'Motivo devolución', 'Fecha creación', 'Última actualización'], tireRows(records))}
</Workbook>`;
  downloadBlob(`\ufeff${workbook}`, 'application/vnd.ms-excel;charset=utf-8', `Reporte_MEV_${fileStamp()}.xls`);
}

export function downloadJson(data, fileName = `Respaldo_MEV_${fileStamp()}.json`) {
  downloadBlob(JSON.stringify(data, null, 2), 'application/json;charset=utf-8', fileName);
}

export function printReport(records, title = 'Reporte consolidado MEV') {
  const summary = calculateSummary(records);
  const rows = records.map(record => `<tr>
    <td>${htmlEscape(recordLabel(record._collection))}</td>
    <td>${htmlEscape(record.codigoActivo)}</td>
    <td>${htmlEscape(record.tipoServicio || record.tipoIntervencion || record.estadoGeneral || '')}</td>
    <td>${htmlEscape(readingLabel(record))}</td>
    <td>${htmlEscape(record.nombreTecnico || record.emailUsuario || '')}</td>
    <td>${htmlEscape(record.estadoRegistro)}</td>
    <td>${htmlEscape(formatDate(record.fechaCreacion || record.fechaActualizacion))}</td>
  </tr>`).join('');
  const printable = `<!doctype html><html lang="es"><head><meta charset="utf-8"><title>${htmlEscape(title)}</title><style>
    @page{size:A4 landscape;margin:12mm}body{font-family:Arial,sans-serif;color:#17252a;font-size:11px}h1{font-size:22px;color:#0f4c5c;margin:0}p{color:#52666c}.kpis{display:grid;grid-template-columns:repeat(6,1fr);gap:7px;margin:18px 0}.kpi{border:1px solid #ccdadd;border-radius:7px;padding:9px}.kpi b{display:block;font-size:19px;color:#0f4c5c}.kpi span{font-size:9px;text-transform:uppercase}table{width:100%;border-collapse:collapse}th,td{border:1px solid #ccdadd;padding:6px;text-align:left;vertical-align:top}th{background:#0f4c5c;color:white}tr:nth-child(even){background:#f3f6f7}.footer{margin-top:12px;font-size:9px;color:#6b7c80}@media print{button{display:none}}
  </style></head><body><h1>${htmlEscape(title)}</h1><p>Generado el ${htmlEscape(formatDate(new Date()))}. Registros incluidos: ${records.length}.</p>
  <div class="kpis">
    <div class="kpi"><b>${summary.totalMantenimientos}</b><span>Mantenimientos</span></div>
    <div class="kpi"><b>${summary.totalHuellas}</b><span>Tomas de huella</span></div>
    <div class="kpi"><b>${summary.huellasCriticas}</b><span>Huellas críticas</span></div>
    <div class="kpi"><b>${summary.totalIntervenciones}</b><span>Intervenciones</span></div>
    <div class="kpi"><b>${summary.enviados}</b><span>Pendientes</span></div>
    <div class="kpi"><b>${summary.aprobados}</b><span>Aprobados</span></div>
  </div>
  <table><thead><tr><th>Tipo</th><th>Activo</th><th>Servicio / intervención</th><th>Lectura</th><th>Técnico</th><th>Estado</th><th>Fecha</th></tr></thead><tbody>${rows || '<tr><td colspan="7">No existen registros para los filtros seleccionados.</td></tr>'}</tbody></table>
  <div class="footer">MEV Mantenimiento Web · Documento generado para control operativo.</div><script>window.addEventListener('load',()=>setTimeout(()=>window.print(),250));<\/script></body></html>`;
  const popup = window.open('', '_blank');
  if (!popup) throw new Error('El navegador bloqueó la ventana de impresión. Habilita las ventanas emergentes e intenta otra vez.');
  popup.opener = null;
  popup.document.open();
  popup.document.write(printable);
  popup.document.close();
}
