#Copy Cpu Util
cp /home/pcube/*.txt .
scp pcube@10.129.2.155:/home/pcube/*.txt .
# scp pcube@10.129.2.201:/home/pcube/*.txt .

#Copy stats for offload clone
cp /var/lib/lxc/load_gen/rootfs/home/ubuntu/multithreaded-blocking-lg/stats.csv stats.csv
cp /var/lib/lxc/load_gen/rootfs/home/ubuntu/multithreaded-blocking-lg/inst.csv inst.csv
# cp /var/lib/lxc/offload_ran2/rootfs/home/ubuntu/turboepc/modular_p4_codes/4chaincode/basic/lg/ran2/clone_bitwise_lg/stats.csv stats_2.csv
# cp /var/lib/lxc/offload_ran3/rootfs/home/ubuntu/turboepc/modular_p4_codes/4chaincode/basic/lg/ran3/clone_bitwise_lg/stats.csv stats_3.csv
# cp /var/lib/lxc/offload_ran4/rootfs/home/ubuntu/turboepc/modular_p4_codes/4chaincode/basic/lg/ran4/clone_bitwise_lg/stats.csv stats_4.csv

#Copy stats for centralized
#cp /var/lib/lxc/ran1/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv stats_1.csv
#cp /var/lib/lxc/ran2/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv stats_2.csv
#cp /var/lib/lxc/ran3/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv stats_3.csv
#cp /var/lib/lxc/ran4/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv stats_4.csv
#cp /var/lib/lxc/ran5/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv stats_5.csv
#cp /var/lib/lxc/ran6/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv stats_6.csv

#Copy sink throughput
#cp /var/lib/lxc/sink/rootfs/home/ubuntu/rinku/sink/throughput.csv sink_throughput.csv

#Change permissions
chmod 777 -R *

