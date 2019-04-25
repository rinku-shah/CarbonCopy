
#Copy Cpu Util
rm /home/pcube/*.txt 
sshpass -p pcube@123 ssh pcube@10.129.2.155 -t 'rm -f /home/pcube/*.txt'

#Delete stats for offload clone
rm /var/lib/lxc/load_gen/rootfs/home/ubuntu/multithreaded-blocking-lg/stats.csv
rm /var/lib/lxc/load_gen/rootfs/home/ubuntu/multithreaded-blocking-lg/inst.csv

#Delete stats for centralized
#rm /var/lib/lxc/ran1/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv
#rm /var/lib/lxc/ran2/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv
#rm /var/lib/lxc/ran3/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv
#rm /var/lib/lxc/ran4/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv
#rm /var/lib/lxc/ran5/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv
#rm /var/lib/lxc/ran6/rootfs/home/ubuntu/p4epc/p4runtime_epc/bitwise_lg/stats.csv

#Change permissions
#chmod 777 -R *

