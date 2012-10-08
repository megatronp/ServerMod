package servermod.crashreporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.pircbotx.Colors;

import servermod.core.ServerMod;
import servermod.util.Util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.CrashReport;
import net.minecraft.src.HttpUtil;
import net.minecraft.src.NetworkListenThread;

public class CrashReporter extends Handler {
	protected final ServerMod sm;
	private VoteRestart voteRestart;
	
	public CrashReporter(ServerMod sm) {
		this.sm = sm;
		if (sm.settings.crash_reporter_vote_restart) voteRestart = new VoteRestart(this);
		NetworkListenThread.logger.addHandler(this);
		
		sm.server.logger.log(Level.INFO, "Crash Reporter: Initialized");
	}
	
	@Override
	public void close() throws SecurityException {
		
	}

	@Override
	public void flush() {
		
	}

	@Override
	public void publish(LogRecord arg0) {
		if (!sm.irc.bot.isConnected()) return;
		
		if (arg0.getMessage().startsWith("Failed to handle packet: ")) {
			sm.irc.bot.sendMessage(sm.settings.irc_channel, Colors.BOLD+"Player crash:"+Colors.BOLD+" "+paste(new CrashReport("Exception while handling packet", arg0.getThrown()).getCompleteReport()));
		} else if (arg0.getMessage().startsWith("This crash report has been saved to: ")) {
			sm.irc.serverCrashed = true;
			
			try {
				String s = Util.readFileToString(new File(arg0.getMessage().substring(37)));
				sm.irc.bot.sendMessage(sm.settings.irc_channel, Colors.BOLD+"General server crash:"+Colors.BOLD+" "+paste(s));
			} catch (Throwable e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				sm.irc.bot.sendMessage(sm.settings.irc_channel, Colors.BOLD+"General server crash:"+Colors.BOLD+" The report was unable to be read.");
			}
			
			if (voteRestart != null) {
				voteRestart.votes.clear();
				voteRestart.voting = true;
				
				sm.irc.bot.sendMessage(sm.settings.irc_channel, "Starting restart vote. Need "+Colors.BOLD+sm.settings.crash_reporter_vote_restart_votes+Colors.BOLD+" votes to restart the server. Type !vote to vote");
			}
		} else if (arg0.getMessage().equals("We were unable to save this crash report to disk.")) {
			sm.irc.serverCrashed = true;
			sm.irc.bot.sendMessage(sm.settings.irc_channel, Colors.BOLD+"General server crash:"+Colors.BOLD+" The report was unable to be saved.");
		}
	}
	
	private static String paste(String text) {
		Map<String,String> map = new HashMap<String,String>();
		/*map.put("api_option", "paste");
		map.put("api_dev_key", "4f409719bd70b09270cc9a9b18d6b947");
		map.put("api_paste_code", text);
		map.put("api_paste_private", "1");
		map.put("api_paste_name", "Crash Report "+(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
		map.put("api_paste_expire_date", "N");
		map.put("api_paste_format", "text");
		map.put("api_user_key", "");*/
		map.put("text", text);
		map.put("title", "Crash Report "+(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
		map.put("name", "ServerMod Crash Reporter");
		map.put("private", "1");
		
		String url;
		try {
			//return HttpUtil.sendPost(new URL("http://pastebin.com/api/api_post.php"), map, false);
			return HttpUtil.sendPost(new URL("http://paste.minecraftforge.net/api/create"), map, false);
		} catch (Throwable e) {
			return "The report was unable to be pasted: "+e;
		}
	}
}
