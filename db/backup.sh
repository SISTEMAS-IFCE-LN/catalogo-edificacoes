#!/bin/sh
set -eu

: "${BACKUP_DIR:=/backup}"
: "${PGDATABASE:?PGDATABASE não definido}"

rollover="$BACKUP_DIR/rollover"
daily="$BACKUP_DIR/daily"
weekly="$BACKUP_DIR/weekly"
mkdir -p "$rollover" "$daily" "$weekly"

ts="$(date +%Y%m%d_%H%M%S)"
day="$(date +%Y%m%d)"
week="$(date +%G-W%V)"

tmp="$rollover/.catalogo_${ts}.dump.tmp"
final="$rollover/catalogo_${ts}.dump"

# 1. dump em arquivo temporário
pg_dump -Fc -f "$tmp"

# 2. só promove para .dump se o pg_dump terminou bem
mv "$tmp" "$final"

# 3. cópia diária/semanal fora da rotação
[ -f "$daily/catalogo_${day}.dump" ] || cp "$final" "$daily/catalogo_${day}.dump"
[ -f "$weekly/catalogo_${week}.dump" ] || cp "$final" "$weekly/catalogo_${week}.dump"

# 4. rotação: mantém os 5 backups de deploy mais recentes
ls -1t "$rollover"/catalogo_*.dump | tail -n +6 | xargs -r rm -f --

# 5. retenção dos arquivos históricos
find "$daily"  -name 'catalogo_*.dump' -mtime +7  -delete
find "$weekly" -name 'catalogo_*.dump' -mtime +28 -delete
