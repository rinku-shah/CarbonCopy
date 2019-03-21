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
#include <sstream>
#include <iostream>

#define lg_mac "00:16:3e:f1:c8:77"
#define primary_mac "00:16:3e:bc:c7:99"
#define primary_ip "192.168.2.2"
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

pcpp::Packet createPacket(uint64_t key, uint64_t val) {
	pcpp::Packet newPacket(76);

	// Eth Layer and ipv4 layers are tuned to primary
	pcpp::EthLayer newEthernetLayer(pcpp::MacAddress(lg_mac), pcpp::MacAddress(primary_mac));
	pcpp::IPv4Layer newIPLayer(pcpp::IPv4Address(std::string(lg_ip)), pcpp::IPv4Address(std::string(primary_ip)));
	pcpp::UdpLayer newUdpLayer(53, 53);
	
	uint8_t type_sync = 0x6;
	// uint64_t key = 0x12;
	uint8_t *key1 = (uint8_t *)&key;
	// uint64_t val = 0x94;
	uint8_t *val1 = (uint8_t *)&val;
	uint8_t version = 0x1;

	uint8_t result[18];
	result[0] = type_sync;
	for (int i= 0; i < 8; i ++)
	{
		result[i+1] = key1[i];
		result[i+9] = val1[i];
	}
	result[17] = version;
	uint8_t *payload = (uint8_t*)(&result);

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
	
	pcpp::PayloadLayer newPayload(payload, 34, 0);
            
	newPacket.addLayer(&newEthernetLayer);
	newPacket.addLayer(&newIPLayer);
	newPacket.addLayer(&newUdpLayer);
	newPacket.addLayer(&newPayload);

	return newPacket;
}

int main(int argc, char* argv[]) {
	

	pcpp::Packet pkt = createPacket(0x12, 0x94);

	// IPv4 address of the interface we want to sniff
	std::string interfaceIPAddr = "192.168.1.1";

	// find the interface by IP address
	pcpp::PcapLiveDevice* dev = pcpp::PcapLiveDeviceList::getInstance().getPcapLiveDeviceByIp(interfaceIPAddr.c_str());

	if (dev == NULL)
	{
		printf("Cannot find interface with IPv4 address of '%s'\n", interfaceIPAddr.c_str());
		exit(1);
	}
	// get interface MAC address
	printf("   MAC address:           %s\n", dev->getMacAddress().toString().c_str());

	// open the device before start capturing/sending packets
	if (!dev->open())
	{
		printf("Cannot open device\n");
		exit(1);
	}

	// create the stats object
	PacketStats stats;

	return 0;
}