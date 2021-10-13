#[macro_use] extern crate text_io;

use std::sync::{Mutex,Arc};
use std::collections::LinkedList;
use std::cmp;
//use std::thread;
use std::collections::VecDeque;

const DEBUG: bool = true;

macro_rules! pr {
    ($fmt_string:expr, $($arg:expr),*) => {
            if DEBUG {println!($fmt_string, $($arg),*);}
    };
    
    ($fmt_string:expr) => {
            if DEBUG {println!($fmt_string);}
    };
}

struct Node {
	i:	usize,			/* index of itself for debugging.	*/
	e:	i32,			/* excess preflow.			*/
	h:	i32,			/* height.				*/
}

struct Edge {
        u:      usize,  
        v:      usize,
        f:      i32,
        c:      i32,
}

impl Node {
	fn new(ii:usize) -> Node {
		Node { i: ii, e: 0, h: 0 }
	}

}

impl Edge {
        fn new(uu:usize, vv:usize,cc:i32) -> Edge {
                Edge { u: uu, v: vv, f: 0, c: cc }      
        }

    fn other(&mut self,uid:&usize) -> usize{

    	 //pr!("What what");
		 if *uid == self.u{
		 	self.v
		 }else {
		 	self.u
		 }
		 	

    }
}

fn enter_excess(excess:&mut VecDeque<usize>, nodeindex:&usize,n: &usize) {

	if (*nodeindex != 0) && (*nodeindex != n-1) {
		excess.push_back(*nodeindex);	
	}

}

fn leave_excess(excess:&mut VecDeque<usize>) -> usize {

	excess.pop_front().unwrap()

}

fn push(u:&mut Node,v:&mut Node,e: &mut Edge,excess:&mut VecDeque<usize>, n: &usize){

	pr!("Enter push");
	let d : i32;

	if u.i == e.u {
			d = cmp::min(u.e,e.c - e.f);
			e.f += d;
	}else {
			d = cmp::min(u.e,e.c + e.f);
			e.f -= d;
	}

	u.e -= d;
	v.e += d;

	if u.e > 0 {

		/* still some remaining so let u push more. */

		enter_excess(excess, &u.i,&n);
	}

	if v.e == d {

		/* since v has d excess now it had zero before and
		 * can now push.
		 *
		 */

		enter_excess(excess, &v.i,&n);
	}

	pr!("Pushing {} from {} to {}",d,u.i,v.i);

}

fn relabel(excess:&mut VecDeque<usize>, n: &usize,u:&mut Node) {
	pr!("Relabling");
	u.h += 1;
	enter_excess(excess,&u.i,&n);
}


/*fn push2(u:&mut Node,v:&mut Node,e: &mut Edge,excess:&mut VecDeque<usize>,n: &usize){
	pr!("Yo MOM");
}*/


fn main() {

	let n: usize = read!();		/* n nodes.						*/
	let m: usize = read!();		/* m edges.						*/
	let _c: usize = read!();	/* underscore avoids warning about an unused variable.	*/
	let _p: usize = read!();	/* c and p are in the input from 6railwayplanning.	*/
	let mut node = vec![];
	let mut edge = vec![];
	let mut adj: Vec<LinkedList<usize>> =Vec::with_capacity(n);
	let mut excess: VecDeque<usize> = VecDeque::new();
	let debug = false;
	let mut edge_index: usize;

	let s = 0;
	let _t = n-1;

	println!("n = {}", n);
	println!("m = {}", m);

	for i in 0..n {
		let u:Node = Node::new(i);
		node.push(Arc::new(Mutex::new(u))); 
		adj.push(LinkedList::new());
	}

	for i in 0..m {
		let u: usize = read!();
		let v: usize = read!();
		let c: i32 = read!();
		let e:Edge = Edge::new(u,v,c);
		adj[u].push_back(i);
		adj[v].push_back(i);
		edge.push(Arc::new(Mutex::new(e))); 
	}

	if debug {
		for i in 0..n {
			print!("adj[{}] = ", i);
			let iter = adj[i].iter();

			for e in iter {
				print!("e = {}, ", e);
			}
			println!("");
		}
	}

	pr!("initial pushes");


	node[s].lock().unwrap().h = n as i32;
	let mut iter = adj[s].iter();


	for e in iter {
		//pr!("{}",node[edge[*e].lock().unwrap().other(&s)].lock().unwrap().i);
		//TellMeAJoke();
		let vindex = edge[*e].lock().unwrap().other(&s);
		let init_excess: i32;
		init_excess = edge[*e].lock().unwrap().c;
		node[0].lock().unwrap().e += init_excess;
		push(&mut node[s].lock().unwrap(),&mut node[vindex].lock().unwrap(),&mut edge[*e].lock().unwrap(),&mut excess,&n);

	}	

	
	// Push to all of Sources edges
	
	
	let mut u: usize;
	let mut v: usize;
	let mut f: i32;
	let mut cap :i32;

	pr!("Starting to go through Excess");
	while !excess.is_empty() {
		let mut b :i32;
		u = leave_excess(&mut excess); //excess.pop_front().unwrap();
		v = n;
		edge_index = 0;

		//pr!("New Node with id {}",u);
		
		//index out of bounds on last node
		iter = adj[u].iter();

		for e in iter{
			edge_index = *e;
			//pr!("New Edge");
			if u == edge[*e].lock().unwrap().u{
				v = edge[*e].lock().unwrap().v;
				b = 1;
			}else{
				v = edge[*e].lock().unwrap().u;
				b = -1;
			}

			f = edge[*e].lock().unwrap().f;
			cap = edge[*e].lock().unwrap().c;

			//pr!("now to see if push or relable");

			if (node[u].lock().unwrap().h > node[v].lock().unwrap().h) && (b*f < cap) {
				//pr!("now to break");
				break;
			}else {
				//pr!("now relabling");
				v = n;
			}

		}

		if v != n {
				push(&mut node[u].lock().unwrap(),&mut node[v].lock().unwrap(),&mut edge[edge_index].lock().unwrap(),&mut excess,&n);
			}else{
				relabel(&mut excess, &n,&mut node[u].lock().unwrap());
			}
	}

	println!("f = {}", node[n-1].lock().unwrap().e);

}
