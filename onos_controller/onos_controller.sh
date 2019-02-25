scp $1 pcube@10.129.2.155:~/

ssh -tt pcube@10.129.2.155 << EOF
  scp $1 ubuntu@10.0.3.95:~/
EOF
