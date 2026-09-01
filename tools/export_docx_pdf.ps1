param(
    [Parameter(Mandatory = $true)]
    [string[]]$Documents
)

$word = $null
try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = 0

    foreach ($inputPath in $Documents) {
        $resolved = (Resolve-Path -LiteralPath $inputPath).Path
        $pdfPath = [System.IO.Path]::ChangeExtension($resolved, '.pdf')
        $document = $word.Documents.Open($resolved, $false, $false)
        try {
            $document.Fields.Update() | Out-Null
            foreach ($section in $document.Sections) {
                foreach ($header in $section.Headers) {
                    if ($header.Exists) { $header.Range.Fields.Update() | Out-Null }
                }
                foreach ($footer in $section.Footers) {
                    if ($footer.Exists) { $footer.Range.Fields.Update() | Out-Null }
                }
            }
            $document.Save()
            $document.ExportAsFixedFormat($pdfPath, 17)
            Write-Output $pdfPath
        }
        finally {
            $document.Close($false)
        }
    }
}
finally {
    if ($word -ne $null) {
        $word.Quit()
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}
