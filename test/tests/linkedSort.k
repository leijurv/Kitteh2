struct node{
	bool hasNext
	long value
	node* next
}
struct linked{
	node* first
	bool hasFirst
}
newLinked() linked*{
	res:=(linked*)malloc(sizeof(linked))
	res.hasFirst=false
	return res
}
add(linked* list, long toAdd){
	n:=(node*)malloc(sizeof(node))
	n.value=toAdd
	if !list.hasFirst{
		n.hasNext=false
		list.hasFirst=true
		list.first=n
		return
	}
	n.hasNext=true
	n.next=list.first
	list.first=n
}
removeLowest(linked* list) long{
	this:=list.first
	prev:=(node*)0
	bestPrev:=prev
	lowestValue:=this.value
	for this.hasNext{
		prev=this
		this=this.next
		if this.value < lowestValue{
			lowestValue=this.value
			bestPrev=prev
		}
	}
	if (long)bestPrev==(long)0{
		list.hasFirst=list.first.hasNext
		list.first=list.first.next
		return lowestValue
	}
	bestPrev.hasNext=bestPrev.next.hasNext
	bestPrev.next=bestPrev.next.next
	return lowestValue
}
sort(long* ptr,int size) {
	l:=newLinked()
	for i:=0; i<size; i++{
		add(l,ptr[i])
	}
	for i:=0; i<size; i++{
		ptr[i]=removeLowest(l)
	}
}
r(int* seed,int max)int{
	*seed=(*seed*5023+257)%2147483647
	return *seed%max
}
main(){
	seed:=(int*)malloc(sizeof(int))
	*seed=12359
	num:=10
	toSort:=(long*)malloc(num*sizeof(long))
	for i:=0; i<num; i++{
		toSort[i]=(long)r(seed,100)
	}
	for i:=0; i<num; i++{
		print(toSort[i])
	}
	sort(toSort,num)
	for i:=0; i<num; i++{
		print(toSort[i])
	}
}
