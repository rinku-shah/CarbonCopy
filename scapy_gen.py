import sys
from scapy.all import *
from random import randint
import time


dst_ip = sys.argv[1]
num_packets = int(sys.argv[2])
read_isto_write = int(sys.argv[3])
delay = sys.argv[4]
print("==== Total num of packets sent will be %d =====" % num_packets*read_isto_write)

class ReadData(Packet):
    name = "data "
    fields_desc = [ByteField("type_sync", 0),
                   IntField("key1", 0),
                   IntField("key2", 0),
                   IntField("key3", 0),
                   IntField("key4", 12),
                   IntField("val1", 0),
                   IntField("val2", 0),
                   IntField("val3", 0),
                   IntField("value", 94),
                   ByteField("version", 1)]

class WriteData(Packet):
    name = "data "
    fields_desc = [ByteField("type_sync", 2),
                   IntField("key1", 0),
                   IntField("key2", 0),
                   IntField("key3", 0),
                   IntField("key4", 12),
                   IntField("val1", 0),
                   IntField("val2", 0),
                   IntField("val3", 0),
                   IntField("value", 11),
                   ByteField("version", 1)]


def sendRead():
    key = randint(0,1000)
    val = key + 15
    print("---- sending Read packet with key:%s and value:%d ----" % (key, val))
    sr1(Ether()/IP(dst=dst_ip)/UDP()/ReadData(key4=key,value=val), iface="eth2", timeout=2)

def sendWrite():
    key = randint(0,1000)
    val = key + 15
    print("---- Write packet sent with key:%s and value:%d ----" % (key, val))
    sr1(Ether()/IP(dst=dst_ip)/UDP()/WriteData(key4=key,value=val), iface="eth2", timeout=2)

for i in range(0,int(num_packets)):
    for j in range(0,int(read_isto_write)):
        sendRead()
        time.sleep(float(delay))
    sendWrite()
    time.sleep(float(delay))
