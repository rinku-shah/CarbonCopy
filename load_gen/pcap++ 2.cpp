#include "stdlib.h"
#include <string>
#include "Packet.h"
#include "EthLayer.h"
#include "VlanLayer.h"
#include "IPv4Layer.h"
#include "TcpLayer.h"
#include "HttpLayer.h"
#include "UdpLayer.h"
#include "PayloadLayer.h"
#include "PcapLiveDeviceList.h"
#include <in.h>
#include <sys/time.h> 
#include <sstream>
#include <iostream>
#include <stdio.h>
#include <limits.h>

#define lg_mac "00:16:3e:f1:c8:77"
#define gateway_mac "00:16:3e:f5:9c:b0"
#define gateway_ip "192.168.1.2"
#define lg_ip "192.168.1.1"

struct PacketStats
{
	int ethPacketCount;
	int ipv4PacketCount;
	int ipv6PacketCount;
	int tcpPacketCount;
	int udpPacketCount;
	int dnsPacketCount;
	int httpPacketCount;
	int sslPacketCount;
	int keys[100000];
	int vals[100000];
	int offset;
	int writep;
	int wr_cnt;
	int read_cnt;
	double rl;
	double wl;
	double exp_interval;


	/**
	 * Clear all stats
	 */
	void clear() { rl = 0; wl = 0; wr_cnt = 0; read_cnt = 0; offset = 1; ethPacketCount = 0; ipv4PacketCount = 0; ipv6PacketCount = 0; tcpPacketCount = 0; udpPacketCount = 0; tcpPacketCount = 0; dnsPacketCount = 0; httpPacketCount = 0; sslPacketCount = 0; }

	/**
	 * C'tor
	*/
	PacketStats() { clear(); 
		for (int i=0; i < writep; i++) {
			keys[i] = i + offset;
			vals[i] = i + offset;
		}

	}

	void reinit() {
		for (int i=0; i < writep; i++) {
			keys[i] = i + offset;
			vals[i] = i + offset;
		}		
	}

	/**
	 * Collect stats from a packet
	 */
	void consumePacket(pcpp::Packet& packet)
	{
		if (packet.isPacketOfType(pcpp::Ethernet))
			ethPacketCount++;
		if (packet.isPacketOfType(pcpp::IPv4))
			ipv4PacketCount++;
		if (packet.isPacketOfType(pcpp::IPv6))
			ipv6PacketCount++;
		if (packet.isPacketOfType(pcpp::TCP))
			tcpPacketCount++;
		if (packet.isPacketOfType(pcpp::UDP))
			udpPacketCount++;
		if (packet.isPacketOfType(pcpp::HTTP))
			httpPacketCount++;
		if (packet.isPacketOfType(pcpp::SSL))
			sslPacketCount++;
	}

	/**
	 * Print stats to console
	 */
	void printToConsole()
	{
		// printf("Ethernet packet count: %d\n", ethPacketCount);
		// printf("IPv4 packet count:     %d\n", ipv4PacketCount);
		// printf("IPv6 packet count:     %d\n", ipv6PacketCount);
		// printf("TCP packet count:      %d\n", tcpPacketCount);
		// printf("UDP packet count:      %d\n", udpPacketCount);
		// printf("DNS packet count:      %d\n", dnsPacketCount);
		// printf("HTTP packet count:     %d\n", httpPacketCount);
		// printf("SSL packet count:      %d\n", sslPacketCount);
		printf("No. of reads : %d\n", read_cnt);
		printf("No. of writes : %d\n", wr_cnt);
		printf("Avg throughput %f\n", (read_cnt + wr_cnt)/exp_interval);
		printf("Avg read latency %f\n", rl/read_cnt);
		printf("Avg write latency %f\n", wl/wr_cnt);
	}

	void incr_offset(int off) {
		offset += off;
	}


};
// We'l count only UDP Packets

void createPayload(uint8_t* payload, uint32_t key, uint32_t val, uint8_t type_sync) {
	
	// Eth Layer and ipv4 layers are tuned to primary
	
	// uint64_t key = 0x12;
	// printf("type : %d \n", type_sync);
	key = htonl(key);
	uint8_t *key1 = (uint8_t *)&key;
	// uint64_t val = 0x94;
	val = htonl(val);
	uint8_t *val1 = (uint8_t *)&val;
	uint8_t version = 0x1;

	uint8_t result[10];
	result[0] = type_sync;
	for (int i= 3; i >= 0; i--)
	{
		// printf("%d ", key1[i]);
		result[i+1] = key1[i];

		// printf("%d \n", val1[i]);
		result[i+5] = val1[i];
	}
	result[9] = version;
	memcpy(payload, &result, sizeof(result));	 
	
	// fprintf(stderr, "Payload created\n");

}

static void onPacketArrives(pcpp::RawPacket* packet, pcpp::PcapLiveDevice* dev, void* cookie)
{
	// extract the stats object form the cookie
	PacketStats* stats = (PacketStats*)cookie;

	// parsed the raw packet
	pcpp::Packet parsedPacket(packet);

	// collect stats from packet
	stats->consumePacket(parsedPacket);
}


/**
 * a callback function for the blocking mode capture which is called each time a packet is captured
 */
static bool onPacketArrivesBlockingMode(pcpp::RawPacket* packet, pcpp::PcapLiveDevice* dev, void* cookie)
{
	// extract the stats object form the cookie
	PacketStats* stats = (PacketStats*)cookie;

	// parsed the raw packet
	pcpp::Packet parsedPacket(packet);

	// collect stats from packet
	stats->consumePacket(parsedPacket);
	pcpp::PayloadLayer* payload = parsedPacket.getLayerOfType<pcpp::PayloadLayer>();
	if (payload == NULL) {
		fprintf(stderr, "No Payload Layer\n");
		return false;
	}

	pcpp::UdpLayer* udp = parsedPacket.getLayerOfType<pcpp::UdpLayer>();
	if (payload == NULL) {
		fprintf(stderr, "No UDP Layer\n");
		return false;
	}

	
	uint8_t* payload_data = payload->getPayload();
	uint16_t srcPort = ntohs((udp->getUdpHeader())->portSrc);
	//fprintf(stderr, "srcPort : %d\n", srcPort);
	//fprintf(stderr, "%d\n", sizeof(payload->getPayload()));
	// fprintf(stderr, "Payload : %d\n", (int)*payload_data);
	int type_sync = (int)*payload_data;
	// for (int j = 0; j < 4;j ++)
	// {
	// 	fprintf(stderr, "Payload key : %d\n", payload_data[j+1]);
	// 	fprintf(stderr, "Payload val : %d\n", payload_data[j+5]);	
	// }
	int key = (payload_data[4] | (payload_data[3] << 8) | (payload_data[2] << 16) | (payload_data[1] << 24));
	// fprintf(stderr, "Payload key : %d\n", key);
	
	int val = (int)(payload_data[8] | (payload_data[7] << 8) | (payload_data[6] << 16) | (payload_data[5] << 24));
	// fprintf(stderr, "Payload val : %d\n", val);
	
	// return false means we don't want to stop capturing after this 
	if ((type_sync == 3) && (srcPort == 12345 || srcPort ==12346)) {
		stats->wr_cnt += 1;
		// fprintf(stderr, "Write Packet rcvd\n");
		return true;
	}
	else if ((type_sync == 1) && (srcPort == 12345 || srcPort ==12346)) {
		stats->read_cnt += 1;
		// fprintf(stderr, "Read Packet rcvd\n");
		return true;
	}
	else {
		return false;
	}
}

int main(int argc, char* argv[]) {
	

	// IPv4 address of the interface we want to sniff
	std::string interfaceIPAddr = "192.168.1.1";
	// int interval = 2;

	// find the interface by IP address
	pcpp::PcapLiveDevice* dev = pcpp::PcapLiveDeviceList::getInstance().getPcapLiveDeviceByIp(interfaceIPAddr.c_str());
	// pcpp::PcapLiveDevice* dev1 = pcpp::PcapLiveDeviceList::getInstance().getPcapLiveDeviceByIp(interfaceIPAddr.c_str());

	if (dev == NULL)
	{
		fprintf(stderr, "Cannot find interface with IPv4 address of '%s'\n", interfaceIPAddr.c_str());
		exit(1);
	}
	// get interface MAC address
	printf("   MAC address:           %s\n", dev->getMacAddress().toString().c_str());

	// open the device before start capturing/sending packets
	if (!dev->open())
	{
		fprintf(stderr, "Cannot open device\n");
		exit(1);
	}

	// create the stats object
	PacketStats stats;

	// Async packet capture with a callback function (Non-blocking)
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	// printf("\nStarting async capture...\n");

	// start capture in async mode. Give a callback function to call to whenever a packet is captured and the stats object as the cookie
	// dev->startCapture(onPacketArrives, &stats);

	// sleep for 10 seconds in main thread, in the meantime packets are captured in the async thread
	// PCAP_SLEEP(interval);

	// Sending single packets
	// ~~~~~~~~~~~~~~~~~~~~~~

	
	int num_packets = 100, writep = 90, num_batches = 1, wr_threshold = 10000;
	int captured;
	stats.writep = writep * num_batches;
	int timeout = 2;
	stats.exp_interval = 5.0*6.0f;
	// int rw_seq[] = {0x2, 0x6, 0x2, 0x6, 0x2, 0x6, 0x2, 0x6};
	// int key_seq[] = {12, 12, 14, 14, 16, 16, 18, 18};
	// int val_seq[] = {94, 95, 96, 97, 98, 99, 100, 101};

	pcpp::Packet pkt;
	
	pcpp::EthLayer newEthernetLayer(pcpp::MacAddress(lg_mac), pcpp::MacAddress(gateway_mac), PCPP_ETHERTYPE_IP);
	
	pcpp::IPv4Layer newIPLayer(pcpp::IPv4Address(std::string(lg_ip)), pcpp::IPv4Address(std::string(gateway_ip)));
	newIPLayer.getIPv4Header()->protocol = pcpp::PACKETPP_IPPROTO_UDP;
	newIPLayer.getIPv4Header()->ipVersion = 4;
	newIPLayer.getIPv4Header()->timeToLive = 64;
	newIPLayer.getIPv4Header()->totalLength = htons(38);
	
	pcpp::UdpLayer newUdpLayer(12345, 12346);
	newUdpLayer.getUdpHeader()->length = htons(18);

	uint8_t* payload = (uint8_t*)malloc(10);
	
	pcpp::PayloadLayer newPayload(payload, 10, 0);
            
	pkt.addLayer(&newEthernetLayer);
	pkt.addLayer(&newIPLayer);
	pkt.addLayer(&newUdpLayer);
	pkt.addLayer(&newPayload);


	struct timeval stop, start;
	struct timeval exp_start, exp_end;
	int flag = 0;
	// int key = 0x0,value = 0x0;
	gettimeofday(&exp_start, NULL);
	for (int biter = 1; biter <= num_batches; biter++) {
		int num = 0;
		while (num < num_packets) {

			captured = 0;

			uint8_t* payload_new = newPayload.getPayload();
			if(num < writep){	
				createPayload(payload_new, stats.keys[num], stats.vals[num], 0x2);
			}
			else {
				int somenum = (rand() % (writep)) + 1; 
				// fprintf(stderr, "somenum : %d\n", somenum);
				createPayload(payload_new, stats.keys[somenum], 129, 0x6);	
			}
			
			gettimeofday(&start, NULL);
			if (!dev->sendPacket(&pkt))
			{
				fprintf(stderr, "Couldn't send packet\n");
				exit(1);
			}

			//fprintf(stderr, "Packet sent!\n");
			//printf("\nStarting capture in blocking mode...\n");
	
			captured = dev->startCaptureBlockingMode(onPacketArrivesBlockingMode, &stats, timeout);

			if (captured == 1) {
				gettimeofday(&stop, NULL);
				if (num < writep) {
					fprintf(stderr, "Write Packet rcvd\n");
					stats.wl += (double)((long)stop.tv_usec - (long)start.tv_usec)/(double)1000;
				}
				else {
					fprintf(stderr, "Read Packet rcvd\n");
					stats.rl += (double)((long)stop.tv_usec - (long)start.tv_usec)/(double)1000;
				}
				// diff_t = difftime(end_t, start_t);
				//stats.printToConsole();
				fprintf(stderr, "RTT time : %llu\n", (long long int)stop.tv_usec - (long long int)start.tv_usec);

			}

			num += 1;
			// stats.clear();
			gettimeofday(&exp_end, NULL);
			if ((exp_end.tv_sec - exp_start.tv_sec) > stats.exp_interval) {
				flag = 1;
				break;
			}
		
		}

		if (flag == 1)
			break;
		if (stats.offset > wr_threshold) {
			stats.offset = 1;
		}
		else {
			stats.incr_offset(writep);
		}
		stats.reinit();

	
	}


	// pcpp::Packet pktr = createPacket(12, 9, 0x6);

	// if (!dev->sendPacket(&pktr))
	// {
	// 	printf("Couldn't send packet\n");
	// 	exit(1);
	// }
	
	// stop capturing packets
	// dev->stopCapture();

	// print results
	// printf("Results:\n");
	stats.printToConsole();

	// clear stats
	// stats.clear();


	return 0;
}