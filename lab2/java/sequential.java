import java.util.Scanner;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.LinkedList;

import java.io.*;



class Graph {

	int	s;
	int	t;
	int	n;
	int	m;
	Node	excess;		// list of nodes with excess preflow
	Node	node[];
	Edge	edge[];

	Graph(Node node[], Edge edge[])
	{
		this.node	= node;
		this.n		= node.length;
		this.edge	= edge;
		this.m		= edge.length;
	}

	void enter_excess(Node u)
	{
		if (u != node[s] && u != node[t]) {
			u.next = excess;
			excess = u;
		}
	}

	Node other(Edge a, Node u)
	{
		if (a.u == u)	
			return a.v;
		else
			return a.u;
	}

	void relabel(Node u)
	{
		u.h += 1;
	}

	void push(Node u, Node v, Edge e)
	{
		int		d;	/* remaining capacity of the edge. */

		System.Out.println("Enter push")
		//pr("push from %d to %d: ", id(g, u), id(g, v));
		//pr("f = %d, c = %d, so ", e->f, e->c);
		
		if (u == e.u) {
			d = Math.min(u.e, e.c - e.f);
			e.f += d;
		} else {
			d = Math.min(u.e, e.c + e.f);
			e.f -= d;
		}

		//pr("pushing %d\n", d);

		u.e -= d;
		v.e += d;

		/* the following are always true. */

		assert d >= 0;
		assert u.e >= 0;
		assert Math.abs(e.f) <= e.c;

		if (u.e > 0) {

			/* still some remaining so let u push more. */

			enter_excess(u);
		}

		if (v.e == d) {

			/* since v has d excess now it had zero before and
			 * can now push.
			 *
			 */

			enter_excess(v);
		}
	}

	int preflow(int s, int t)
	{
		ListIterator<Edge>	iter;
		int				b;
		Edge			a;
		Node			u;
		Node			v;
		
		this.s = s;
		this.t = t;
		node[s].h = n;

		iter = node[s].adj.listIterator();
		while (iter.hasNext()) {
			a = iter.next();

			node[s].e += a.c;

			push(node[s], other(a, node[s]), a);
		}

		while (excess != null) {
			u = excess;
			v = null;
			a = null;
			excess = u.next;

			iter = u.adj.listIterator();
			while (iter.hasNext()) {
				a = iter.next();
			}

			if (v != null)
				push(u, v, a);
			else
				relabel(u);
		}

		return node[t].e;
	}
}

class Node {
	int	h;
	int	e;
	int	i;
	Node	next;
	LinkedList<Edge>	adj;

	Node(int i)
	{
		this.i = i;
		adj = new LinkedList<Edge>();
	}
}

class Edge {
	Node	u;
	Node	v;
	int	f;
	int	c;

	Edge(Node u, Node v, int c)
	{
		this.u = u;
		this.v = v;
		this.c = c;

	}
}

class Preflow {
	public static void main(String args[])
	{
		double	begin = System.currentTimeMillis();
		Scanner s = new Scanner(System.in);
		int	n;
		int	m;
		int	i;
		int	u;
		int	v;
		int	c;
		int	f;
		Graph	g;

		n = s.nextInt();
		m = s.nextInt();
		s.nextInt();
		s.nextInt();
		Node[] node = new Node[n];
		Edge[] edge = new Edge[m];

		for (i = 0; i < n; i += 1)
			node[i] = new Node(i);

		for (i = 0; i < m; i += 1) {
			u = s.nextInt();
			v = s.nextInt();
			c = s.nextInt(); 
			edge[i] = new Edge(node[u], node[v], c);
			node[u].adj.addLast(edge[i]);
			node[v].adj.addLast(edge[i]);
		}

		g = new Graph(node, edge);
		f = g.preflow(0, n-1);
		double	end = System.currentTimeMillis();
		System.out.println("t = " + (end - begin) / 1000.0 + " s");
		System.out.println("f = " + f);
	}
}
