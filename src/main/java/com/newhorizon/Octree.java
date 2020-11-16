package com.newhorizon;
import org.joml.Vector3f;
import org.joml.Vector3i;
import java.util.Arrays;
import java.util.LinkedList;

public class Octree {

    final Node root;

    //Dimension is in Chunks

    public Octree(short dimension){
        this.root = new Node((short)0,(short)0,(short)0,dimension,null,"a");
    }

    public LinkedList<Node> searchChunk(Vector3f position){
        LinkedList<Node> chunks = root.getParts(position);
        return chunks;
    }


    public class Node{
        boolean isRoot;
        boolean isLeaf;

        Node[] children;

        short indexX;
        short indexY;
        short indexZ;

        short span;
        Node parent;

        String id;

        public Node(short indexX, short indexY, short indexZ, short span, Node parent,String id){

            this.indexX = indexX;
            this.indexY = indexY;
            this.indexZ = indexZ;

            this.span = span;
            this.parent = parent;
            this.children = new Node[8];

            isRoot = (parent == null);
            isLeaf = (span <= 1);

            this.id = id;

        }

        public LinkedList<Node> getParts(Vector3f position){
            LinkedList<Node> chunks = new LinkedList<>();

            if(getDistance(position)>((this.span*LookupTable.CHUNKSIZE)*Math.sqrt(3))*0.3f){
                chunks.add(this);
                return chunks;
            }else{
                if(!isLeaf){
                    Node[] children = new Node[8];
                    short childSpan = (short) (span/2);
                    children[0]=(new Node(indexX,indexY,indexZ,childSpan,this,this.id+"a"));
                    children[1]=(new Node((short)(indexX+childSpan),indexY,indexZ,childSpan,this,this.id+"b"));
                    children[2]=(new Node(indexX,indexY, (short) (indexZ+childSpan),childSpan,this,this.id+"c"));
                    children[3]=(new Node((short) (indexX+childSpan),indexY,(short)(indexZ+childSpan),childSpan,this,this.id+"d"));

                    children[4]=(new Node(indexX,(short)(indexY+childSpan),indexZ,childSpan,this,this.id+"e"));
                    children[5]=(new Node((short)(indexX+childSpan),(short)(indexY+childSpan),indexZ,childSpan,this,this.id+"f"));
                    children[6]=(new Node(indexX,(short)(indexY+childSpan),(short)(indexZ+childSpan),childSpan,this,this.id+"g"));
                    children[7]=(new Node((short)(indexX+childSpan),(short)(indexY+childSpan),(short)(indexZ+childSpan),childSpan,this,this.id+"h"));

                    for(Node child : children){
                        chunks.addAll(child.getParts(position));
                    }
                }else{
                    chunks.add(this);
                }

            }
            return chunks;
        }

        public float getDistance(Vector3f position){
            //Closest point of chunk
            float dx = Math.max(Math.max(this.indexX*LookupTable.CHUNKSIZE - position.x, position.x - (this.indexX+span)*LookupTable.CHUNKSIZE),0);
            float dy = Math.max(Math.max(this.indexY*LookupTable.CHUNKSIZE - position.y, position.y - (this.indexY+span)*LookupTable.CHUNKSIZE),0);
            float dz = Math.max(Math.max(this.indexZ*LookupTable.CHUNKSIZE - position.z, position.z - (this.indexZ+span)*LookupTable.CHUNKSIZE),0);
            return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
            //Distance to center of chunk
            //return position.distance((this.indexX+(span/2))*LookupTable.CHUNKSIZE,(this.indexY+(span/2))*LookupTable.CHUNKSIZE , (this.indexZ+(span/2))*LookupTable.CHUNKSIZE);
        }

        public LinkedList<Node> getSiblings(){
            if(!isRoot){
                Node[] temp = parent.getChildren();
                LinkedList<Node> test = new LinkedList<>();
                test.addAll(Arrays.asList(temp));

                test.remove(this);

                return test;
            }
            return null;
        }

        public Node[] getChildren(){
            if(!isLeaf){
                return children;
            }
            return null;

        }
        public LinkedList<Node> getRelatives(LinkedList<Node> relatives){
            if(!isRoot){
                relatives.addAll(getSiblings());
                return parent.getRelatives(relatives);
            }
            return relatives;

        }

        public boolean isInside(Vector3i chunk){

            if(chunk.x >= this.indexX && chunk.x < (this.indexX+this.span)){
                if(chunk.y >= this.indexY && chunk.y < (this.indexY+this.span)){
                    if(chunk.z >= this.indexZ && chunk.z < (this.indexZ+this.span)){
                        return true;
                    }
                }
            }

            return false;
        }

        public Node search(Vector3i chunk){
            Node res = null;

            if(isInside(chunk) && isLeaf){
                return this;
            }

            if(isInside(chunk) && !isLeaf) {
                for (Node child : this.children){
                    if(res == null){
                        res = child.search(chunk);
                    }
                }
            }

            return res;

        }


        public String toString(){

            return "This is Chunk ("+this.indexX+", "+this.indexY+", "+this.indexZ+"). Span = "+this.span;

        }


    }







}
