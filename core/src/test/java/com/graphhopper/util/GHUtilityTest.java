/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;


import com.graphhopper.coll.GHIntLongHashMap;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.*;
import com.graphhopper.routing.ch.CHRoutingAlgorithmFactory;
import org.junit.jupiter.api.Test;

import static com.graphhopper.util.GHUtility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class GHUtilityTest {

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));
    }

    //N - 1
    // retourne le node adjacent a un autre node qui partage la meme arete
    /*@Test
    public void getNodeAdjTest(){
        Directory dir = new RAMDirectory();
        BaseGraph graph = new BaseGraph(dir, true, true, 100, 8);  // Création de l'instance de BaseGraph
        graph.create(500);  // Initialisation avec une taille de stockage de 500

        // Ajouter des arêtes avec des distances
        graph.edge(1, 2).setDistance(2);
        graph.edge(1, 3).setDistance(40);
        EdgeIteratorState edgeState = graph.edge(2, 3).setDistance(10);

        int edgeId = edgeState.getEdge();
        int result = getAdjNode(graph, edgeId, 2);
        assertEquals(3,result);

    }*/

    //N - 2 - inutile a enlever
    // voir si 2 aretes partagent un noeud
    @Test
    public void getCommonNodeTest(){
        Directory dir = new RAMDirectory();
        BaseGraph graph = new BaseGraph(dir, true, true, 100, 8);  // Création de l'instance de BaseGraph
        graph.create(500);  // Initialisation avec une taille de stockage de 500

        // Ajouter des arêtes avec des distances
        graph.edge(1, 2).setDistance(2);
        EdgeIteratorState edgeState2 = graph.edge(1, 3).setDistance(40);
        EdgeIteratorState edgeState = graph.edge(2, 3).setDistance(10);

        int edgeId = edgeState.getEdge();
        int edgeId2 = edgeState2.getEdge();
        int result = getCommonNode(graph, edgeId, edgeId2);
        assertEquals(3,result);
    }

    //N - 3 - inutile a enlever
    @Test
    public void testGetCommonNodeThrowsIllegalArgumentException() {
        Directory dir = new RAMDirectory();
        BaseGraph graph = new BaseGraph(dir, true, true, 100, 8);  // Création de l'instance de BaseGraph
        graph.create(500);  // Initialisation avec une taille de stockage de 500

        // Ajouter des arêtes avec des distances
        graph.edge(1, 2).setDistance(2);
        EdgeIteratorState edgeState2 = graph.edge(1, 3).setDistance(40);
        EdgeIteratorState edgeState = graph.edge(2, 3).setDistance(10);

        int edgeId = edgeState.getEdge();
        int edgeId2 = edgeState2.getEdge();

        try {
            int result = getCommonNode(graph, edgeId, edgeId);
            fail("expected exception was not occured.");
        } catch(IllegalArgumentException e) {
            //if execution reaches here,
            //it indicates this exception was occured.
            //so we need not handle it.
            System.out.println("Excepted exception is handled");
        }
    }

    //N - 4
    // mentionner linspi : https://stackoverflow.com/questions/156503/how-do-you-assert-that-a-certain-exception-is-thrown-in-junit-tests#:~:text=Using%20ExpectedException%20you%20could%20call%20N
    @Test
    public void testExceptionEdgeIterator(){
            Directory dir = new RAMDirectory();
            BaseGraph graph = new BaseGraph(dir, true, true, 100, 8);  // Création de l'instance de BaseGraph
            graph.create(500);  // Initialisation avec une taille de stockage de 500

            // Ajouter des arêtes avec des distances
            graph.edge(1, 2).setDistance(2);
            graph.edge(1, 3).setDistance(40);
            graph.freeze();

            try {
                EdgeIteratorState edgeState = graph.edge(2, 3).setDistance(10);
            } catch (IllegalStateException e) {
                System.out.println("Excepted exception is handled");
            }


        }

    @Test
        public void testGetProblems(){
        Directory dir = new RAMDirectory();
        BaseGraph graph = new BaseGraph(dir, true, true, 100, 8);  // Création de l'instance de BaseGraph
        graph.create(500);  // Initialisation avec une taille de stockage de 500

        try {
            System.out.println(getProblems(graph));
        } catch (IllegalStateException e) {
            System.out.println("Excepted exception is handled");
        }
    }

    // non valide et faux
    /*public void getMinDistTest(){

        //Initialisation d'un graphe pour le contexte de tests.
        Directory dir = new RAMDirectory();
        BaseGraph graph = new BaseGraph(dir, true, true, 100, 8);  // Création de l'instance de BaseGraph
        graph.create(500);  // Initialisation avec une taille de stockage de 500

        // Ajouter des arêtes avec des distances
        graph.edge(1, 2).setDistance(2);
        graph.edge(1, 3).setDistance(40);
        graph.edge(2, 3).setDistance(10);

        // Tester getMinDist() -> ajouter assert
        double minDist = getMinDist(graph, 1, 3);
        assertEquals(12,minDist);
    }*/


    }







