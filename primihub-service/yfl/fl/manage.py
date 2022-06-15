#!/usr/bin/env python
"""Django's command-line utility for administrative tasks."""
import os
import sys
import nacos
import threading
import time
import socket

NACOS_SERVER_ADDRESS = '192.168.0.109'
NACOS_NAMESPACE = 'public'
NACOS_USERNAME = 'nacos'
NACOS_PASSWORD = 'nacos'
APPLICATION_NAME = "fedlearner"


def main():
    """Run administrative tasks."""
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'fl.settings')
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Are you sure it's installed and "
            "available on your PYTHONPATH environment variable? Did you "
            "forget to activate a virtual environment?"
        ) from exc
    execute_from_command_line(sys.argv)


def get_host_ip():
    try:
        s=socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        s.connect(('8.8.8.8',80))
        ip=s.getsockname()[0]
    finally:
        s.close()

    return ip


def nacos_send_heartbeat(client, port):
    while(True):
        client.send_heartbeat(APPLICATION_NAME, get_host_ip(), port, None, 1, None, True)
        time.sleep(5)


def _init_nacos():
    port = 8000
    if len(sys.argv) == 3 and sys.argv[1] == 'runserver':
        port = sys.argv[2].split(":")[1]
    client = nacos.NacosClient(NACOS_SERVER_ADDRESS, namespace=NACOS_NAMESPACE, username=NACOS_USERNAME, password=NACOS_PASSWORD)
    client.add_naming_instance(APPLICATION_NAME, get_host_ip(), port, None, 1, None, True, True, True)
    heartbeat=threading.Thread(target=nacos_send_heartbeat, args=(client, port,))
    heartbeat.start()


if __name__ == '__main__':
    _init_nacos()
    main()
