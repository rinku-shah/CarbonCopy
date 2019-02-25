scp $1 pcube@10.129.2.201:~/

ssh -tt pcube@10.129.2.201 << EOF
  scp $1 ubuntu@10.0.3.70:~/
EOF

exit
