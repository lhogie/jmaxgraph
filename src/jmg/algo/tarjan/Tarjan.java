/******************************************************************************
 *  Compilation:  javac Tarjan.java
 *  Execution:    Java Tarjan graph.txt
 *  Dependencies: jmaxgraph-0.3.0.jar toools.jar fastutil-8.1.0.jar java4unix.jar
 *  Data files:   data/digraph/tinyDG.txt
 *
 *  Compute the strongly-connected components of a digraph using 
 *  Tarjan's algorithm tuned according Robert Sedgewick
 *
 *  Runs in O(E + V) time.
 *
 *  % java Tarjan data/digraph/tinyDG.txt
 *  5 components
 *  1 
 *  0 2 3 4 5
 *  9 10 11 12
 *  6 8
 *  7 
 *  
 *  % java -cp ../jar/jmg.jar:../jar/toools.jar:../jar/fastutil-8.1.0.jar:
 *  ../jar/java4unix.jar jmg.algo.Tarjan tinyDG.txt
 *
 ******************************************************************************/

package jmg.algo.tarjan;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.LinkedList;

import jmg.Graph;
import jmg.MatrixAdj;
import toools.io.file.RegularFile;

/**
 * The {@code TarjanSCC} class represents a data type for determining the strong
 * components in a digraph. The <em>id</em> operation determines in which strong
 * component a given vertex lies; the <em>areStronglyConnected</em> operation
 * determines whether two vertices are in the same strong component; and the
 * <em>count</em> operation determines the number of strong components.
 * 
 * The <em>component identifier</em> of a component is one of the vertices in
 * the strong component: two vertices have the same component identifier if and
 * only if they are in the same strong component.
 * 
 * <p>
 * This implementation uses Tarjan's algorithm. The constructor takes time
 * proportional to <em>V</em> + <em>E</em> (in the worst case), where <em>V</em>
 * is the number of vertices and <em>E</em> is the number of edges. Afterwards,
 * the <em>id</em>, <em>count</em>, and <em>areStronglyConnected</em> operations
 * take constant time. For alternate implementations of the same API, see
 * {@link KosarajuSharirSCC} and {@link GabowSCC}.
 * <p>
 * For additional documentation, see
 * <a href="https://algs4.cs.princeton.edu/42digraph">Section 4.2</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class Tarjan {

	private boolean[] marked; // marked[v] = has v been visited?
	private int[] id; // id[v] = id of the strong component containing v
	private int[] low; // low[v] = low number of any node reachable from v through DFS subtree
	private int pre; // preorder number counter = DFS count
	private int count; // number of strongly-connected components
	private ArrayDeque<Integer> stack;

	/**
	 * Computes the strong components of the digraph {@code G}.
	 * 
	 * @param G
	 *            the digraph
	 */
	public Tarjan(Graph G) {
		marked = new boolean[G.getNbVertices()]; // initialized to False
		stack = new ArrayDeque<Integer>();
		id = new int[G.getNbVertices()];
		low = new int[G.getNbVertices()];
		for (int v = 0; v < G.getNbVertices(); v++) {
			if (!marked[v])
				dfs(G, v);
		}
		// check that id[] gives strong components
		// optional
		// assert check(G);
	}

	private void dfs(Graph G, int v) {
		MatrixAdj outs = G.out.mem;
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		/*
		 * min is not stored in an array as it is the case in the original algorithm.
		 * It's value is only required for v and the last w visited. See also the
		 * algorithm of Cheriyan-Mehlhorn-Gabow.
		 */
		stack.push(v);
		for (int w : outs.b[v]) {
			if (!marked[w])
				dfs(G, w);
			if (low[w] < min)
				min = low[w];
		}
		if (min < low[v]) { // low[v] != min, not the root of a SCC
			low[v] = min;
			return;
		}
		/* new SCC found with v as root */
		int w;
		do {
			w = stack.pop();
			id[w] = count;
			low[w] = outs.b.length;
			/* may not be lower than another */
		} while (w != v);
		count++;
	}

	/**
	 * Returns the number of strong components.
	 * 
	 * @return the number of strong components
	 */
	public int count() {
		return count;
	}

	/**
	 * Are vertices {@code v} and {@code w} in the same strong component?
	 * 
	 * @param v
	 *            one vertex
	 * @param w
	 *            the other vertex
	 * @return {@code true} if vertices {@code v} and {@code w} are in the same
	 *         strong component, and {@code false} otherwise
	 * @throws IllegalArgumentException
	 *             unless {@code 0 <= v < V}
	 * @throws IllegalArgumentException
	 *             unless {@code 0 <= w < V}
	 */
	public boolean stronglyConnected(int v, int w) {
		validateVertex(v);
		validateVertex(w);
		return id[v] == id[w];
	}

	/**
	 * Returns the component id of the strong component containing vertex {@code v}.
	 * 
	 * @param v
	 *            the vertex
	 * @return the component id of the strong component containing vertex {@code v}
	 * @throws IllegalArgumentException
	 *             unless {@code 0 <= v < V}
	 */
	public int id(int v) {
		validateVertex(v);
		return id[v];
	}

	// does the id[] array contain the strongly connected components?
	// optional
	private boolean check(Graph G) {
		TransitiveClosure tc = new TransitiveClosure(G);
		for (int v = 0; v < G.getNbVertices(); v++) {
			for (int w = 0; w < G.getNbVertices(); w++) {
				if (stronglyConnected(v, w) != (tc.reachable(v, w) && tc.reachable(w, v)))
					return false;
			}
		}
		return true;
	}

	// throw an IllegalArgumentException unless {@code 0 <= v < V}
	private void validateVertex(int v) {
		int V = marked.length;
		if (v < 0 || v >= V)
			throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
	}

	/**
	 * Unit tests the {@code Tarjan} data type.
	 *
	 * @param args
	 *            the command-line arguments
	 * @throws IOException
	 * 
	 * @michel : demo with data/digraph/tinyDG.txt
	 *         https://algs4.cs.princeton.edu/42digraph/
	 */
	public static void main(String[] args) throws IOException {

		// Graph G = new Graph();

		// // load a graph
		// // gets the out adjacency lists for all vertices
		// MatrixAdj outs = G.out.mem;
		// outs.b = DirectedGNP.out(20, 0.1, new Random(), false, 1);
		// int nbVertices = outs.b.length;
		// for (int u = 0; u < nbVertices; ++u) {
		// // Cout.debug(outs.b[u]);
		// System.out.println(u + ": " + Arrays.toString(outs.b[u]));
		// }

		// demo Tarjan
		// read in digraph from command-line argument
		Graph G = new Graph();
		G.out.mem.from(new RegularFile(args[0]));

		Tarjan scc = new Tarjan(G);

		// number of connected components
		int m = scc.count();
		System.out.println(m + " components");

		// compute list of vertices in each strong component
		LinkedList<Integer>[] components = new LinkedList[m];
		// Queue<Integer>[] components = (Queue<Integer>[]) new Queue[m];
		for (int i = 0; i < m; i++) {
			components[i] = new LinkedList<Integer>();
		}
		for (int v = 0; v < G.getNbVertices(); v++) {
			components[scc.id(v)].add(v);
		}

		// print results
		for (int i = 0; i < m; i++) {
			for (int v : components[i]) {
				System.out.print(v + " ");
			}
			System.out.println();
		}
	}
}
/******************************************************************************
 * Copyright 2002-2016, Robert Sedgewick and Kevin Wayne.
 *
 * This file is part of algs4.jar, which accompanies the textbook
 *
 * Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne, Addison-Wesley
 * Professional, 2011, ISBN 0-321-57351-X. http://algs4.cs.princeton.edu
 *
 *
 * algs4.jar is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * algs4.jar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * algs4.jar. If not, see http://www.gnu.org/licenses.
 ******************************************************************************/