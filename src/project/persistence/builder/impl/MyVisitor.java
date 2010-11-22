package project.persistence.builder.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.visitors.NodeVisitor;

import project.client.persistence.Message;
import project.client.persistence.User;

public class MyVisitor extends NodeVisitor
{
	private boolean bInterested = false;
	private String author = null;
	private String date   = null;
	private String msgId  = null;
	private Message parent = null;
	private List<Message> messages;
	
	private Tag endTag = null;
	private Tag parentEndTag = null;
	
	//
	// used internally to store intermediate results
	//
	private List<String>  foundSoFar;
	
	private static Pattern pattern = null;
	
	static {
		/*
		On May 22, 5:03 am, abhiram
		@gmail.com> wrote:

					 */

		String p = "On [a-zA-Z0-9:,@> \n\r\t/]*";
		pattern = Pattern.compile(p);
	}
	
    public MyVisitor ()
    {
    	messages = new LinkedList<Message> ();
    	foundSoFar = new LinkedList<String> ();
    }

    public void visitTag (Tag tag)
    {
    	/*
    	if (tag.getText().contains("?hide_quotes=no#msg_")) {
    		if (parentId == null) {
    			parentId = tag.getText();
    			parentId = parentId.substring(parentId.indexOf("?hide_quotes=no#msg_") + "?hide_quotes=no#msg_".length());
    			parentId = parentId.substring(0, parentId.indexOf("\""));
    		}
    		//System.out.println ("Parent id: " + parentId);
    	}
    	*/
    	    	
    	if (tag.getTagName().equalsIgnoreCase("input")) {
    		if (tag.getText().startsWith("input id=\"hdn_author\"")) {
    			try {
    				author = tag.getText();
    				author = author.substring(author.indexOf("value=\"") + "value=\"".length());
    				author = author.substring(0, author.indexOf("<"));
    				author = author.trim();
    				//System.out.println ("Author: " + author);
    			} catch (StringIndexOutOfBoundsException e) {
    				// TODO ignore this, the author is not valid
    			}
    			
    			foundSoFar.clear();
    		} else if (tag.getText().startsWith("input id=\"hdn_date\"")) {
    			try {
    				date = tag.getText();
    				date = date.substring(date.indexOf("value=\"") + "value=\"".length());
    				date = date.substring(0, date.length() - 1);
    				//System.out.println ("Date: " + date);
    			} catch (StringIndexOutOfBoundsException e) {
    				// TODO ignore this, the date is not valid (code was unable to find it)
    			}
    		}
    	} else if (tag.getTagName().equalsIgnoreCase("div")) {
    		
    		//System.out.println (tag.getText());
    		if (tag.getText().startsWith("div class=\"msg wdth100\"")) {
    			// now we get the id of message
    			
    			try {
    				msgId = tag.getText();
    				msgId = msgId.substring(msgId.indexOf("id=\"") + "id=\"".length());
    				msgId = msgId.substring(0, msgId.indexOf("\""));
    				msgId = msgId.substring(4);
    				
    				//System.out.println ("msg id: " + msgId);
    			} catch (StringIndexOutOfBoundsException e) {
    				// TODO ignore this
    			}
    		}
    		else if (tag.getText().startsWith("div ID=qhide_") && parentEndTag == null) {
				parentEndTag = tag;    			
    		}
    	}
    	
    	if (tag.getText().matches("div id=\"inbdy\"")) {
    		bInterested = true;
    		//System.out.println ("------------------------------------------------------------------------------");
    		//System.out.println ("Author: " + author);
    		foundSoFar.clear();
    		endTag = tag.getEndTag();
    	}
    	
    }

    public void visitStringNode (Text string)
    {
    	//System.out.println (string.getText());
    	
    	if (parentEndTag != null && parent == null) {
    		String text = string.toPlainTextString();
    		
    		text = text.trim();
    		if (text.indexOf("wrote:") != -1) {
    			//System.out.println ("Candidate n: " + text);    			
    			text = text.substring(text.indexOf("wrote:") + "wrote:".length());
    			//System.out.println ("Candidate n': " + text);
    			
    			text = text.trim();
    	   		if (text.indexOf('>') != -1) {
        			text = text.substring(text.indexOf('>') + 1);
        			if (text.indexOf('>') != -1) {
        				text = text.substring(0, text.indexOf('>'));
        			}
        		}    			
    		}    		
    		
    		text = text.trim();
    		if (text.indexOf('>') != -1) {
    			text = text.substring(text.indexOf('>') + 1);
    			if (text.indexOf('>') != -1) {
    				text = text.substring(0, text.indexOf('>'));
    			}
    		}
    		
    		
    		text = text.trim();
    		parent = this.findParent(text);
    	}
    	
    	if (bInterested) {
    		//System.out.println (string.getText());
    		String txt = string.getText();
    		//txt = txt.replaceAll("&gt;", ">");
    		//txt = txt.replaceAll("&lt;", "<");
    		//txt = txt.replaceAll("&quot;", "\"");
    		//txt = txt.replaceAll("&apos;", "'");
    		//txt = txt.replaceAll("&amp;", "`");
    		//txt = txt.replaceAll("&#xd;", "");
    		
    		if (txt.length() > 3) {
    			//foundSoFar.add(txt);

    			
    			//System.out.println ("------------------------------------");

    			String[] tokens = txt.split("\n");
    			String fmt = "";
    			for (String t : tokens) {
    				t = t.trim();
    				if (!t.startsWith(">")) { 
    					fmt += t + "\n";
    				} else {
    					// 
    				}
    			}
    			
    			fmt.replaceAll("- Hide quoted text -", "");
    			foundSoFar.add(fmt);
    		}
    	}
    }

	public void visitEndTag(Tag tag) {
		
		//String html1 = tag.getParent().toHtml();
		//System.out.println (html1);
		
		if (tag.equals(parentEndTag)) {
			// finished parent
			parentEndTag = null;
		}
		
		if (tag.equals(endTag)) {
			bInterested = false;
			
			Iterator<String> i = foundSoFar.iterator();
			String strMsg = "";
			while (i.hasNext()) {
				strMsg += i.next();
			}
			
			if (strMsg.indexOf("- Hide quoted text -") != -1) {
				strMsg = strMsg.substring(0, strMsg.indexOf("- Hide quoted text -"));
			}
			

			Matcher m = pattern.matcher(strMsg);
			if (m.find()) {
				String str = m.group();
				strMsg = strMsg.substring(0, strMsg.indexOf(str));
				//System.out.println (m.group());
			}
			//else 
				//System.out.println ("No match found");
			
			//
			// create a new owner
			User owner = new User ();
			owner.setName(author);
			
			Message msg = new Message ();
			msg.setContent(strMsg);
			msg.setUser(owner);
			msg.setPublishDate(date);
			msg.setUrl(msgId);
			//if (parent != null)
			msg.setParent(parent);
			//else if (messages.size() != 0)
		    //	msg.setParent(messages.get(0));
			
			/*
			System.out.println ("Author  : " + msg.getUser().getName());
			System.out.println ("Date    : " + msg.getPublishDate());
			System.out.println ("Id      : " + msg.getUrl());
			if (msg.getParent() != null)
				System.out.println ("Parent  : " + msg.getParent().getUrl() + "(" + getMessageIdx(msg.getParent().getUrl()) + ")");
			else
				System.out.println ("Parent  : " + "no parent");
			System.out.println ("Content : \n" + msg.getContent());
			System.out.println ("--------------------------------------------------");
					*/	
			//
			//
			msgId = null;
			endTag = null;
			parentEndTag = null;
			parent = null;
			
			//
			// add the new message
			messages.add(msg);
			
		}
	}

	public List<Message> getMessages() {
		return messages;
	}
	
	
	private Message findParent (String text) {
		if (text == null || messages.size() == 0) {
			return null;
		}
		
		if (text.length() == 0)
			return null;
		
		//System.out.println ("Finding parent with text : " + text + "(#messages = " + messages.size() + ")");
		
		Iterator<Message> i = messages.iterator();
		int idx = 0;
		while (i.hasNext()) {
			Message m = i.next();
			if (m.getContent().indexOf(text) != -1) {
				//System.out.println ("Found parent : " + idx);
				return m;
			}
			idx ++;
		}

		//System.out.println ("No parent found!");
		return null;
	}
	
	private int getMessageIdx (String url) {
		Iterator<Message> i = messages.iterator();
		int idx = 0;
		while (i.hasNext()) {
			if (i.next().getUrl().equals(url))
				return idx;
			idx ++;
		}
		
		return -1;
	}
}
