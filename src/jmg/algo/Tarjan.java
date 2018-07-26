package jmg.algo;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import jmg.Graph;
import jmg.MatrixAdj;
import jmg.gen.DirectedGNP;

public class Tarjan {

	private boolean[] marked; // marked[v] = has v been visited?
	private int[] id; // id[v] = id of strong component containing v
	private int[] low; // low[v] = low number of v
	private int pre; // preorder number counter
	private int count; // number of strongly-connected components
	private ArrayDeque<Integer> stack;

	/**
	 * Computes the strong components of the digraph {@code G}.
	 * 
	 * @param G
	 *            the digraph
	 */
	public Tarjan(Graph G) {
		marked = new boolean[G.getNbVertices()];
		stack = new ArrayDeque<Integer>();
		id = new int[G.getNbVertices()];
		low = new int[G.getNbVertices()];
		for (int v = 0; v < G.getNbVertices(); v++) {
			if (!marked[v])
				dfs(G, v);
		}
		// check that id[] gives strong components
		assert check(G);
	}

	private void dfs(Graph G, int v) {
		MatrixAdj outs = G.out.mem;
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		stack.push(v);
		for (int w : outs.b[v]) {
			if (!marked[w])
				dfs(G, w);
			if (low[w] < min)
				min = low[w];
		}
		if (min < low[v]) {
			low[v] = min;
			return;
		}
		int w;
		do {
			w = stack.pop();
			id[w] = count;
			low[w] = outs.b.length;
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

	public static void main(String[] args) {
		
		Graph G = new Graph();
		
		// load a graph
		// gets the out adjacency lists for all vertices
		MatrixAdj outs = G.out.mem;
		outs.b = DirectedGNP.out(20, 0.1, new Random(), false, 1);
		int nbVertices = outs.b.length;
		for (int u = 0; u < nbVertices; ++u) {
			// Cout.debug(outs.b[u]);
			System.out.println(u + ": " + Arrays.toString(outs.b[u]));
		}
		
		// demo Tarjan
		
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
