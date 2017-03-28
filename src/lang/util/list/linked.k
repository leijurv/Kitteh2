struct node{
	bool hasNext
	void* value
	node* next
	free(){ // TODO maybe free values? but the values aren't always pointers... hmm
		if this.hasNext{
			free(this.next)
		}
	}
}
struct list{
	node* head
	bool hasFirst
	free(){
		if this.hasFirst{
			free(this.head)
		}
	}
	push(void* toAdd){
		n:=(node*)malloc(sizeof(node))
		n.value=toAdd
		n.next=this.head
		n.hasNext=this.hasFirst
		this.head=n
		this.hasFirst=true
	}
	pop()void*{
		tmp:=this.head.value
		this.head=this.head.next
		return tmp
	}
	append(void* toAdd){
		n:=(node*)malloc(sizeof(node))
		n.value=toAdd
		n.hasNext=false
		if this.hasFirst{
			tmp:=this.head
			for tmp.hasNext{
				tmp=tmp.next
			}
			tmp.hasNext=true
			tmp.next=n
		}else{
			this.hasFirst=true
			this.head=n
		}
	}
	printAsLongs(){
		print("Printing linked list")
		writeByte(10)
		if !this.hasFirst{
			return
		}
		tmp:=this.head
		print((long)tmp.value)
		for tmp.hasNext{
			tmp=tmp.next
			print((long)tmp.value)
		}
	}
}
newList() list*{
	res:=(list*)malloc(sizeof(list))
	res.hasFirst=false
	return res
}

