package com.test.core;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
public class Test1 {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader( new File("assembleur.txt")));
		BufferedWriter bw= new BufferedWriter(new FileWriter("temp.txt"));
		HashMap<String,Integer> adds = new HashMap<String,Integer>();//用于存储跳转时跳转的标签及行号
		String line = null;//用于存储从文本文件中读取出来的每一行
		String write = null;//用于存储每行处理结果
		String outputFileName = "output1.txt"; //输出文件的文件名，可自己指定文件路径，和文件名称
		int n = 0;//用于记录行号
		try {
			while ((line = br.readLine())!=null){
				/**处理标签,':'前为标签名称,后面为操作**/
				if(line.contains(":")){
					String parts[ ] = line.split(":");
					adds.put(parts[0].trim(), n*4);
					line = parts[1];
				}
				/**处理空行**/
				if(line.equals("")){
					bw.write("\n");
					continue;
				}
				/**处理注释,直接回车换行**/
				if(line.contains("#")){
					if(line.startsWith("#")){
						bw.write("\n");
						continue;
					}
					else line = line.substring(0, line.indexOf("#")-1);
				}
				/**处理操作语句**/
				//删除语句前后的空格
				line = line.trim();
				write = 4*n+"\t: ";
				if(line.startsWith("read"))
					write += "Scall 0";
				else if(line.startsWith("write"))
					write += "Scall 1";
				else if(line.startsWith("stop"))
					write += "Stop";
				else if(line.startsWith("jmp"))
					write += "Jmp ( " + params("jmp",line.subSequence(3, line.length()).toString())+" )";	
				else if(line.startsWith("braz"))
					write += "Braz ( " + params("braz",line.subSequence(4, line.length()).toString())+" )";	
				else if(line.startsWith("add"))
					write += "Op ( Add , " + params("add",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("sub"))
					write += "Op ( Sub , " + params("sub",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("mult"))
					write += "Op ( Mult , " + params("mult",line.subSequence(4, line.length()).toString())+" )";
				else if(line.startsWith("div"))
					write += "Op ( Div , " + params("div",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("and"))
					write += "Op ( And , " + params("and",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("or"))
					write += "Op ( Or , " + params("or",line.subSequence(2, line.length()).toString())+" )";
				else if(line.startsWith("xor"))
					write += "Op ( Xor , " + params("xor",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("shl"))
					write += "Op ( Shl , " + params("shl",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("shr"))
					write += "Op ( Shr , " + params("shr",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("slt"))
					write += "Op ( Slt , " + params("slt",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("sle"))
					write += "Op ( Sle , " + params("sle",line.subSequence(3, line.length()).toString())+" )";
				else if(line.startsWith("load"))
					write += "Op ( Load , " + params("load",line.subSequence(4, line.length()).toString())+" )";
				else if(line.startsWith("store"))
					write += "Op ( Store , " + params("store",line.subSequence(5, line.length()).toString())+" )";
				bw.write(write + "\n");
				n++;
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			br.close();
			bw.close();
		}
		/**替换文件中的变量**/
		BufferedReader br1 = new BufferedReader(new FileReader( new File("temp.txt")));
		BufferedWriter bw1= new BufferedWriter(new FileWriter(outputFileName));
		while((line = br1.readLine())!=null)
		{
			//某行需要跳转到一个函数，而该函数的地址使用的是变量代替的
			if(line.contains("{")){
				String var = line.substring(line.indexOf("{")+1, line.indexOf("}"));
				line = line.replace("{"+var+"}",String.valueOf(adds.get(var)));
			}
			bw1.write(line+"\n");
		}
		bw1.flush();
		br1.close();
		bw1.close();
		/**删除临时文件**/
		File f =new File("temp.txt");
		f.delete();
	}
	/**
	 * 处理操作的参数
	 * @param subSequence 带处理的参数列表
	 * @return 处理后的参数
	 */
	private static String params(String op,String argStr) {
		argStr = argStr.trim();
		String args[] = argStr.split(",");
		String result ="";
		for(int i=0;i<args.length;i++)
		{
			args[i]=args[i].trim();
			if(args[i].startsWith("ra")){
				if ((op.equals("jmp") && i==0)||(args.length==3&&i==1))
					args[i] = "Reg 31";
				else
					args[i] = "31";
			}else if(args[i].startsWith("sp")){
				if ((op.equals("jmp") && i==0)||(args.length==3&&i==1))
					args[i] = "Reg 30";
				else
					args[i] = "30";
			}else if(args[i].startsWith("r")){
				if ((op.equals("jmp") && i==0)||(args.length==3&&i==1))
					args[i] = "Reg "+ args[i].substring(1, args[i].length());
				else
					args[i] = args[i].substring(1, args[i].length());
			}
			//由于按时不知道函数的入口地址，所以用变量代替其地址，到时候记得进行替换
			else if(args[i].startsWith("fact")){
				if(op.equals("jmp") && i==0)
					args[i] = "Imm {"+args[i]+"}";
				else
					args[i] = "{"+args[i]+"}";
			}else{
				if(op.equals("store")||op.equals("load")||op.equals("shl")||op.equals("shr"))
					args[i] = String.valueOf(Integer.valueOf(args[i])/4);
				args[i] = "Imm " + args[i];
			}
			result += args[i];
			if(i<args.length-1)
				result += " , ";
		}
		return result;
	}
}