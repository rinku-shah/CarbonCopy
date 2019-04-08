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
#include <sstream>
#include <iostream>
#include <unistd.h>


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


	/**
	 * Clear all stats
	 */
	void clear() { ethPacketCount = 0; ipv4PacketCount = 0; ipv6PacketCount = 0; tcpPacketCount = 0; udpPacketCount = 0; tcpPacketCount = 0; dnsPacketCount = 0; httpPacketCount = 0; sslPacketCount = 0; }

	/**
	 * C'tor
	 */
	PacketStats() { clear(); }

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
		printf("Ethernet packet count: %d\n", ethPacketCount);
		printf("IPv4 packet count:     %d\n", ipv4PacketCount);
		printf("IPv6 packet count:     %d\n", ipv6PacketCount);
		printf("TCP packet count:      %d\n", tcpPacketCount);
		printf("UDP packet count:      %d\n", udpPacketCount);
		printf("DNS packet count:      %d\n", dnsPacketCount);
		printf("HTTP packet count:     %d\n", httpPacketCount);
		printf("SSL packet count:      %d\n", sslPacketCount);
	}
};
// We'l count only UDP Packets

pcpp::Packet createPacket(uint32_t key, uint32_t val, uint8_t type_sync) {
	pcpp::Packet newPacket(52);

	// Eth Layer and ipv4 layers are tuned to primary
	pcpp::EthLayer newEthernetLayer(pcpp::MacAddress(lg_mac), pcpp::MacAddress(gateway_mac), PCPP_ETHERTYPE_IP);
	pcpp::IPv4Layer newIPLayer(pcpp::IPv4Address(std::string(lg_ip)), pcpp::IPv4Address(std::string(gateway_ip)));
	newIPLayer.getIPv4Header()->protocol = pcpp::PACKETPP_IPPROTO_UDP;
	newIPLayer.getIPv4Header()->ipVersion = 4;
	newIPLayer.getIPv4Header()->timeToLive = 64;
	newIPLayer.getIPv4Header()->totalLength = htons(38);
	// fprintf(stderr, "%d\n", newIPLayer.getIPv4Header()->ipVersion);
	pcpp::UdpLayer newUdpLayer(12345, 4000);
	newUdpLayer.getUdpHeader()->length = htons(18);

	// uint64_t key = 0x12;
	// key = htons(key);
	uint8_t *key1 = (uint8_t *)&key;
	// uint64_t val = 0x94;
	// val = htons(val);
	uint8_t *val1 = (uint8_t *)&val;
	uint8_t version = 0x1;

	uint8_t result[10];
	result[0] = type_sync;
	for (int i= 3; i >= 0; i--)
	{
		printf("%d ", key1[i]);
		result[i+1] = key1[i];

		printf("%d \n", val1[i]);
		result[i+5] = val1[i];
	}
	result[9] = version;
	uint8_t *payload = (uint8_t*)(&result);

	fprintf(stderr, "Payload created\n");

	// std::stringstream ss1;
	// std::stringstream ss2;
	// std::stringstream ss3;
	// std::stringstream ss4;
	// ss1 << "0x" << std::hex << type_sync;
	// ss2 << "0x" << std::hex << key;
	// ss3 << "0x" << std::hex << val;
	// ss4 << "0x" << std::hex << version;
	// char *str;
	// std::string s1 = ss1.str();
	// std::string s2 = ss2.str();
	// std::string s3 = ss3.str();
	// std::string s4 = ss4.str();
	// strcat(str, s1.c_str());
	// strcat(str, s2.c_str());
	// strcat(str, s3.c_str());
	// strcat(str, s4.c_str());

	pcpp::PayloadLayer newPayload(payload, 10, 0);

	newPacket.addLayer(&newEthernetLayer);
	newPacket.addLayer(&newIPLayer);
	newPacket.addLayer(&newUdpLayer);
	newPacket.addLayer(&newPayload);

	fprintf(stderr, "Packet created\n");

	return newPacket;
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

	// return false means we don't want to stop capturing after this callback
	return false;
}

int main(int argc, char* argv[]) {


	// IPv4 address of the interface we want to sniff
	std::string interfaceIPAddr = "192.168.1.1";

	// int interval = 2;

	// find the interface by IP address
	pcpp::PcapLiveDevice* dev = pcpp::PcapLiveDeviceList::getInstance().getPcapLiveDeviceByIp(interfaceIPAddr.c_str());

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
	int base = 100;
	int i=1,j=1;
	while(true){

			int count = 10;
			while(count--){
				pcpp::Packet pktw = createPacket(i, count + 10, 0x2);
				if (!dev->sendPacket(&pktw))
				{
					fprintf(stderr, "Couldn't send packet\n");
					exit(1);
				}
				fprintf(stderr, "write count = %d\n",i);
				i++;

				usleep(10000);

				fprintf(stderr, "Packet sent!\n");
			}

			count = 10;
			while(count--){
				pcpp::Packet pktr = createPacket(j, 100, 0x6);

				if (!dev->sendPacket(&pktr))
				{
					printf("Couldn't send packet\n");
					exit(1);
				}
				usleep(10000);
				fprintf(stderr, "read count = %d\n",j);
				j++;

			}
			// if(i==100){
			// 	fprintf(stderr, "**Band ho gaya*******************************\n");
			// 	usleep(20000000);
			// }
			base += 100;
	}

	// stop capturing packets
	// dev->stopCapture();

	// print results
	 printf("Results:\n");
	 stats.printToConsole();

	// clear stats
	 stats.clear();


	return 0;
}
