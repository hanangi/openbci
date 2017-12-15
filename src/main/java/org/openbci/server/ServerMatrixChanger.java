package org.openbci.server;

import org.openbci.astart.ProjectStarter;

public class ServerMatrixChanger extends Server {

		public ServerMatrixChanger() {
			this.port = ProjectStarter.getConf().getValueInt("tcpIpServerInputPort") ;
		}

		@Override
		public int getType() {
			return Server.TYPE_MATRIX_CHANGER;
		}
	}