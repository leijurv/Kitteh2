struct list{
	void** contents
	int length
	int capacity
	free(){
		if this.capacity!=0{
			free(this.contents)
		}
	}
	ensureCap(int cap){
		if cap<=this.capacity{
			return
		}
		cap=cap*2 // overallocate for better performance
		tmp:=(void**)malloc(cap*sizeof(void*))
		c:=this.contents
		for i:=this.length-1; i>=0; i--{
			tmp[i]=c[i]
		}
		free(c)
		this.capacity=cap
		this.contents=tmp
	}
	prepend(void* toAdd){
		this.ensureCap(this.length+1)
		c:=this.contents
		for i:=this.length-1; i>=0; i--{
			c[i+1]=c[i]
		}
		c[0]=toAdd
		this.length=this.length+1
	}
	append(void* toAdd){
		l:=this.length+1
		this.ensureCap(l)
		this.length=l
		this.contents[l-1]=toAdd
	}
	printAsLongs(){
		print("Printing array list")
		writeByte(10)
		for i:=0; i<this.length; i++{
			print((long)this.contents[i])
		}
	}
}
newList() list*{
	res:=(list*)malloc(sizeof(list))
	res.capacity=0
	res.length=0
	res.contents=(void**)0
	return res
}