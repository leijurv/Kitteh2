main(){
a:="main(){&a:=&for i:=0; i<11; i++{&if a[i]==(byte)38{&writeByte(10)&}else{&writeByte(a[i])&}&}&writeByte(34)&print(a)&writeByte(34)&for i:=11; a[i]!=(byte)0; i++{&if a[i]==(byte)38{&writeByte(10)&}else{&writeByte(a[i])&}&}&}"
for i:=0; i<11; i++{
if a[i]==(byte)38{
writeByte(10)
}else{
writeByte(a[i])
}
}
writeByte(34)
print(a)
writeByte(34)
for i:=11; a[i]!=(byte)0; i++{
if a[i]==(byte)38{
writeByte(10)
}else{
writeByte(a[i])
}
}
}