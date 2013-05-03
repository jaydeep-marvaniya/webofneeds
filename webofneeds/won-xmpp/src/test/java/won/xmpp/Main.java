/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.xmpp;

import won.protocol.model.Need;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * User: Ashkan
 * Date: 30.03.13
 */
public class Main {

    public static void main(String args[]) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader( System.in));

        WONXmppServer server = new WONXmppServerImpl();

        server.start();
        System.out.println("server is running ...");
        System.out.println("predefined  users are set");


        System.out.println("press Enter to terminate");
        reader.readLine();

        server.stop();

        System.out.println("server stopped!");


    }
}
