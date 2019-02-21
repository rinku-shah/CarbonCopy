
cp $1 code.txt
scp code.txt pcube@10.129.2.201:~/

ssh -tt pcube@10.129.2.201 << EOF
  scp code.txt ubuntu@10.0.3.25:~/
EOF

rm code.txt
