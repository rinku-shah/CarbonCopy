 nl basic-inst.csv | tr "," "  "|awk '{print($1*10,$2,$3,$4,$5,$6)}'>basic-inst.dat
nl CC-sync-inst.csv | tr "," "  "|awk '{print($1*10,$2,$3,$4,$5,$6)}'>CC-sync-inst.dat
nl CC-CP-inst.csv | tr "," "  "|awk '{print($1*10,$2,$3,$4,$5,$6)}'> CC-CP-inst.dat


