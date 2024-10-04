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


import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.coll.GHIntLongHashMap;
import com.graphhopper.routing.Path;
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

import java.util.ArrayList;
import java.util.List;

import static com.graphhopper.util.DistanceCalcEarth.DIST_EARTH;
import static com.graphhopper.util.GHUtility.*;
import static java.lang.Math.sqrt;
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
                System.out.println("Expected exception is handled");
            }

            try {
                EdgeIteratorState edgeState = graph.edge(4, 4).setDistance(10);
            } catch (IllegalStateException e){
                System.out.println("Expected exception is handled");
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

    /*
    Nous avons observé que comparePaths, dans ses conditions, ne prend pas en compte qu'un
    Path pourrait avoir un poids, une distance ou un temps nuls.
    Même si, actuellement, la méthode est appelée dans le cadre d'un test (où les valeurs sont
    non-nulles car les objets sont correctement initialisés), il faut quand même qu'elle les
    prenne en compte. Il se peut qu'à un moment donné, un utilisateur fait rouler comparePaths
    sans bien initialiser ses objets, et la méthode devrait le laisser savoir.
 */


    private Graph buildGraph1(){
        Directory dir_p1 = new RAMDirectory();
        BaseGraph graph_p1 = new BaseGraph(dir_p1, false, true, 100, 8);  // Création de l'instance de BaseGraph
        graph_p1.create(500);
        NodeAccess na_p1 = graph_p1.getNodeAccess();
        na_p1.setNode(1, 10, 10); na_p1.setNode(2, 8, 10);
        na_p1.setNode(3, 8, 7); na_p1.setNode(4, 5, 7);
        graph_p1.edge(1,2).setDistance(2);
        graph_p1.edge(2,3).setDistance(4);
        graph_p1.edge(3,4).setDistance(1);

        return graph_p1;
    }

    public Graph buildGraph2(){
        Directory dir_p2 = new RAMDirectory();
        BaseGraph graph_p2 = new BaseGraph(dir_p2, false, true, 100, 8);  // Création de l'instance de BaseGraph
        graph_p2.create(500);
        NodeAccess na_p2 = graph_p2.getNodeAccess();
        na_p2.setNode(1, 10, 10); na_p2.setNode(2, 8, 10);
        na_p2.setNode(3, 8, 7); na_p2.setNode(4, 10, 7);
        graph_p2.edge(1,2).setDistance(2);
        graph_p2.edge(1,3).setDistance(3);
        graph_p2.edge(3,4).setDistance(5);

        return graph_p2;
    }

    /*
        Tester le comportement normal de comparePaths
        On veut s'assurer que dans les conditions où les paths ne sont pas égaux, elle retourne
        la bonne liste.
        Comme la méthode ne teste pas le cas ou les paths sont nuls et elle fail naturellement
        lorsque les inputs sont incorrects, nous testerons seulement le comportement habituel de
        la fonction.
     */
    @Test
    public void testComparePaths_normal(){
        final long seed = System.nanoTime();

        Graph graph_p1 = buildGraph1();
        Graph graph_p2 = buildGraph2();

        Path p1 = new Path(graph_p1);
        Path p2 = new Path(graph_p2);

        // p1 et p2 ont les mêmes nodes, mais pas exactement le même temps & distance (diff < 0.01 & <50)
        // comparePaths devrait retourner une liste vide
        p1.setWeight(1.289200); p1.setDistance(11.001); p1.setTime(500);
        p2.setWeight(1.289203); p2.setDistance(11); p2.setTime(501);
        List<String> output1 = comparePaths(p1,p2,1,4,seed);
        List<String> expected1 = new ArrayList<>();

        // Devrait retourner une liste vide
        assertEquals(expected1, output1);
    }

    /*
        Tester comparePaths lorsqu'elle lance des messages d'erreur ou elle retourne une liste
        indiquant les entrées invalides
     */
    @Test
    public void testComparePaths_errors(){
        // Test wrong weight
        final long seed = System.nanoTime();

        Graph graph_p1 = buildGraph1();
        Graph graph_p2 = buildGraph2();

        Path p1 = new Path(graph_p1);
        Path p2 = new Path(graph_p2);

        // p1 et p2 ont les mêmes nodes, mais pas exactement le même temps & distance (diff < 0.01 & <50)
        // comparePaths devrait retourner une liste vide
        p1.setWeight(1.289200);
        p2.setWeight(1.389203);

        assertThrows(AssertionError.class, () -> {
            comparePaths(p1, p2, 1, 4, seed);
        });

        // p1 et p2 ont les mêmes nodes et des temps & distances (diff > 0.01 & >50)
        // comparePaths devrait retourner une liste non-vide
        p1.setWeight(1.289200); p1.setDistance(12); p1.setTime(500);
        p2.setWeight(1.289203); p2.setDistance(11); p2.setTime(555);
        List<String> output2 = comparePaths(p1,p2,1,4,seed);
        List<String> expected2 = new ArrayList<>();
        expected2.add("wrong distance 1->4, expected: 12.0, given: 11.0");
        expected2.add("wrong time 1->4, expected: 500, given: 555");

        // Devrait retourner des messages indiquant que l'entrée est invalide
        assertEquals(expected2, output2);
    }


    /*
        La méthode updateDistancesFor est utilisée partout dans le code, surtout dans le but de
        tester les fonctionnements de création de chemins alternatifs et d'algorithmes de routage.
        Elle est donc très pertinente à tester.
        Nous testerons son comportement dans 3 cas séparés pour améliorer la compréhension
        des tests et l'interprétation d'erreurs, si elles surviennent.
        Pour les 2 derniers tests, les valeurs positives ou négatives n'affectent pas les
        calculs des fonctions appelées dans les méthodes, alors nous ne testerons pas pour des
        valeurs positives et négatives séparément
     */

    // Tester d'abord le lancement d'une erreur si on donne seulement une coordonnée à modifier
    @Test
    public void testUpdateDistancesFor_invalidArgs(){
        // Arrange (faire un graphe quelconque)
        Graph g = buildGraph1();

        // Act & assert via un assertThrows
        assertThrows(IllegalArgumentException.class, () -> {
            updateDistancesFor(g, 2, 4000);
        });
    }

    /* Reprendre la méthode de DistanceCalcEuclidian.java pour calculer les distances entre les
        points des graphes qui seront crées
     */
    private double calcDist(double fromY, double fromX, double toY, double toX) {
        double dX = fromX - toX;
        double dY = fromY - toY;
        return sqrt(dX * dX + dY * dY);
    }

    /* Tester ensuite l'autre branche, qui lance une erreur si le graphe est eh 3d mais on envoie
        le mauvais nombre d'arguments.
     */
    @Test
    public void testUpdateDistancesFor_3DInvalidInput(){
        // Arrange - Créer un graphe 3D quelconque & enregistrer valeurs
        Directory dir = new RAMDirectory();
        BaseGraph g = new BaseGraph(dir, true, true, 100, 8);  // Création de l'instance de BaseGraph
        g.create(500);
        NodeAccess na = g.getNodeAccess();

        na.setNode(1, 10, 10, 2); na.setNode(2, 8, 10,3);
        na.setNode(3, 8, 7,0); na.setNode(5, 5 ,1, 2);

        g.edge(1,2);
        g.edge(2,3);
        g.edge(2,4);
        g.edge(3,4);
        g.edge(4,5);

        // Calculer les distances du graphe avec la formule, pour les arêtes associées au noeud 2
        EdgeIterator iter_og = g.createEdgeExplorer().setBaseNode(2);
        while (iter_og.next()) {
            int base = iter_og.getBaseNode();
            int adj = iter_og.getAdjNode();
            double dist = calcDist(na.getLon(base), na.getLat(base), na.getLon(adj), na.getLat(adj));
            iter_og.setDistance(dist);
        }

        assertThrows(IllegalArgumentException.class, () -> {
            updateDistancesFor(g, 2, 4);
        });
    }


}







