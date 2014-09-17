# Floodlight QueuePusher

This is a module to be installed within Floodlight[🔗](http://www.projectfloodlight.org/floodlight/) to facilitate the installation of Queues in Open vSwitches.

## Objective

With the intention of providing an interface inside the Floodlight controller to ease the process of queue creation within an OF enabled switch, in particular the Open vSwitch (OVS), the QueuePusher was created. This entity presents itself as an extension to Floodlight, a module per se, rather than as a standalone application; following this approach allows the QueuePusher to be easily embedded in every Floodlight installation and also to enable Floodlight to handle all the event processing, avoiding the creation of additional overheads on the controller communication.

## Features

- Uses OVSDB protocol to communicate with Open vSwitches
- Complete integration with Floodlight REST API

## Requirements

To successfully install and run Floodlight with the QueuePusher, the following requirements should be met. Though it might work with earlier or later versions of the given software, as well as different releases (like OpenJDK), it has not been tested:

- Oracle Java Virtual Machine ≥ 1.6
- Floodlight == 0.9
- Open vSwitch ≥ 1.9.3 (tested with == 2.0.0 as well)
- curl ≥ 7.22.0

**Note:** Since this is provided as a module, it is at least expected from you to know how to build Floodlight.

## Usage

Assuming that all the above requirements are met:

1. Place the QueuePusher package inside the source of Floodlight
2. That's it!

## Source

If not doing so already, you can download the latest version of the JavaProfiling-tool by cloning the [github](https://github.com/OneSourceConsult/floodlight-queuepusher) repository.

## Copyright

Copyright (c) 2014 OneSource Consultoria Informatica, Lda. [🔗](http://www.onesource.pt)

Copyright (c) 2014 RedZinc [🔗](http://www2.redzinc.net/)

This project has been developed in the scope of the CityFlow project[🔗](http://www.cityflow.eu/) by João Gonçalves and David Palma

## License

Distributed under the Apache 2 license. See ``LICENSE`` for more information.

##About

### OneSource

OneSource is the core institution that supported this work. Regarding queries about further development of custom solutions and consultancy services please contact us by email: **_geral✉️onesource.pt_** or through our website: <http://www.onesource.pt>

OneSource is a Portuguese SME specialised in the fields of data communications, security, networking and systems management, including the consultancy, auditing, design, development and lifetime administration of tailored IT solutions for corporate networks, public-sector institutions, utilities and telecommunications operators.

Our company is a start-up and technological spin-off from Instituto Pedro Nunes (IPN), a non-profit private organisation for innovation and technology transfer between the University of Coimbra and the industry and business sectors. Faithful to its origins, OneSource keeps a strong involvement in R&D activities, participating in joint research projects with academic institutions and industrial partners, in order to be able to provide its customers with state-of-art services and solutions.

### RedZinc

RedZinc was founded in July 2004 by Donal Morris, with a vision to provide quality of service capabilities on the public internet. In September 2004 RedZinc joined the EuQoS research consortium on end to end quality of service over heterogeneous networks.

Between 2004 and 2008 RedZinc focused on research and development on public internet Quality of Service in partnership with leading research institutes including University of Coimbra, University of Bern, Laboratoire D'analyse Et D'architecture Des Systemes, University of Pisa and Warsaw Technical University.
