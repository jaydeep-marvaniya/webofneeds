webofneeds
==========

Web of Needs is a new idea based on old technology, enabling a recent field of research.
Central Point is the need. In contrast to the offer, the need is inadequately represented.
Web of needs tries to fill the gap. 

For further information have a look at: http://www.webofneeds.org

Web of needs is build up of 
- won-node
- won-owner 
- and matching-service.

Requirements for getting started:
- jdk 1.6 or later
- maven 2 or later
- tomcat 6 or later as application-server

To prevent reinventing the wheel every single day, 
we come back to existing (old) technology for the matching services:
- siren                   -> http://siren.sindice.com/ 
- triplestore(virtuoso)   -> http://virtuoso.openlinksw.com/
- solr                    -> http://lucene.apache.org/solr/
- ldspider                -> https://code.google.com/p/ldspider/
__________________________________________________________________________________________

won-node
- all needs and offers will be saved there

won-owner
- needs are managed by the won-owner

matching-service
- the matching-service crawls the won-nodes and looks for need and offer entries with corresponding values.
