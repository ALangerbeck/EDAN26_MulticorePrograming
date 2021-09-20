import java.util.Scanner;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.io.*;

class Graph {

	boolean debug =false;
	int	s;
	int	t;
	int	n;
	int	m;
	int number_Of_Threads = 2;

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

		pr("relabel " + u.i + " now h = " +u.h+  "\n");

		enter_excess(u);
	}

	void work(){
		

		while (excess != null) {
			u = excess;
			v = null;
			a = null;
			excess = u.next;

			iter = u.adj.listIterator();
			while (iter.hasNext()) {
					a = iter.next();
					
					if (u == a.u) {
						v = a.v;
						b = 1;
					} else {
						v = a.u;
						b = -1;
					}
					if (u.h > v.h && b * a.f < a.c){
						break;
					}else{
						v = null;

					}

			}

			if (v != null)
				push(u, v, a);
			else
				relabel(u);
			
		}
	} 

	void push(Node u, Node v, Edge a)
	{
		
		int		d;	/* remaining capacity of the edge. */

		pr("Pushing from " + (u.i) + " to " + (v.i)+" : ");
		pr("f ="+ a.f +", c = " + a.c + ", so ");
		
		if (u == a.u) {
			d = Math.min(u.e, (a.c - a.f));
			a.f += d;
		} else {
			d = Math.min(u.e, a.c + a.f);
			a.f -= d;
		}

		pr("pushing " +d+ "\n");

		u.e -= d;
		v.e += d;

		/* the following are always true. */

		assert d >= 0;
		assert u.e >= 0 ;
		assert Math.abs(a.f) <= a.c ;

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
		pr("Enter Preflow\n");
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

	Thread threads[] = new Thread[number_Of_Threads];
	for (int j = 0; j < number_Of_Threads; j++) {
    threads[j] = new Thread(() -> work());
    threads[j].start();
	}
	for (int j = 0; j < number_Of_Threads; j++) {
    threads[j].join(); //todo add catch exception
}
		

		return node[t].e;
	}

	void pr(String text){
	if(debug){
		System.out.print(text);
		}
	}
}

class Node {
	int	h;
	int	e;
	int	i;
	ReentrantLock lock;
	Node	next;
	LinkedList<Edge>	adj;

	Node(int i)
	{
		this.i = i;
		adj = new LinkedList<Edge>();
		lock = new ReentrantLock();
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
		boolean debug = false;
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