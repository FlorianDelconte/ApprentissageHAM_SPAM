import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.IOException;

public class apprend_filtre {
	private final static String regex = "[\\s\\<\\>\\[\\]\\,\\?\\;\\.\\:\\/\\!\\§\\*\\µ\\$\\£\\^\\¨\\%\\@\\)\\(\\=\\+\\{\\'\\}\\|\\#\\-\\\"\\.\\_\\`\\[\\]\\&\\°\\\\[0-9]]+";

	final public static int epsilon = 1;

	private static int nbSpamApp, nbHamApp;

	//c'est mieux un tableau plutot qu'une liste car on vas faire beaucoup d'accès au tableau
	private static String[] dictionnaire;
	// variable utilisee lors du chargement d'un classifieur existant
	private static int tailleDictionnaire;
	// retient, pour chaque mot du dictionnaire, la probabilité de présence parmi tous les spam/ham
	private static double[] probaPresenceMotSPAM;
	private static double[] probaPresenceMotHAM;	
	//probabilité a priori
	private static double P_Yspam;
	private static double P_Yham;

    public static void main(String[] args) {
        if (args.length == 4) {
            charger_dictionnaire();
            String fileName = System.getProperty("user.dir");
			apprentissage(args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]));
			sauvegarde("" + fileName + "/" + args[0]);

            System.out.println("Classifieur enregistré dans '" + args[0] + "'.");
        } else {
            System.err.println("Usage : java apprend_filtre nom_classifieur dossier_apprentissage nbSpam nbHam");
        }
    }

    /**
	 * Chargement du dictionnaire
	 */
	public static void charger_dictionnaire(){
		System.out.println("Chargement du dictionnaire...");
		//le nom du fichier dictionnaire
		String fileName = System.getProperty("user.dir");
		fileName+="/base/dictionnaire1000en.txt";
		//Buffered reader
		BufferedReader br = null;
		//la ligne courante
		String ligne;
		
		ArrayList<String> dictionnaireListe = new ArrayList<>();

		try{

			br = new BufferedReader(new FileReader(fileName));

			//on parcourt jusqu'a la fin du fichier pour compter le nombre de ligne -> permet d'avoirs la taille du tableau dictionnaire
			while ((ligne = br.readLine()) != null) {
				if(ligne.trim().length()>=3){

					dictionnaireListe.add(ligne.toUpperCase().trim());
				}
			}

			dictionnaire = new String[dictionnaireListe.size()];
			dictionnaire = dictionnaireListe.toArray(dictionnaire);

		}catch(IOException e){
			//On affiche l'exception
			e.printStackTrace();
		}finally {
			//fermeture du buffered Reader
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Termine. Nombre de mots du dictionnaire : "+dictionnaire.length);
	/*	for(int i=0;i<dictionnaire.length;i++){
			System.out.println(dictionnaire[i]);
		}*/
    }
    
	/**
	 * Fonction qui construit un vecteur de présence a partir d'un fichier
	 * @param nomFichier de la forme '/base/baseapp/ham/X.txt' ou '/base/baseapp/spam/X.txt'(avex X un chiffre)
	 * @return un vecteur de presence par rapport au dictionnaire : 1 si le mot est présent, 0 sinon
	 */
	public static int[] lire_message(String nomFichier){

		// System.out.println("lecteur du message "+nomFichier+"...");

		//nombre de mot trouvé dans le fichier
		int nbm=0;
		//le vecteur de présence fait la taille du dictionnaire.
		int [] presence=new int[dictionnaire.length];
		//chemin du repertoire du projet
		String fileName = System.getProperty("user.dir");
		fileName+=nomFichier;
		//Buffered reader
		BufferedReader br = null;
		//la ligne courante
		String ligne;
		try{

			br = new BufferedReader(new FileReader(fileName));

			// on parcourt le fichier, et on regarde
			// si chaque mot est dans le dictionnaire
			
			// On parcourt chaque ligne du fichier
			while ((ligne = br.readLine()) != null ) {	
				// On récupère chaque mot de la ligne en retirant tous les symboles autour du mot (autre que des lettres)
				String[] mots = ligne.split(regex);
				for (String mot : mots) {
								
					// On met le mot en majuscules et on enlève les espaces autour
					mot = mot.toUpperCase().trim();
					boolean trouve = false;
					int i = 0;
					// Tant qu'on a pas trouve ce mot dans le dictionnaire
					// et qu'on a pas dépassé la taille du dictionnaire
					while (!trouve && i < dictionnaire.length) {
						// Si le mot est le meme que le mot courant du dictionnaire
						if (mot.equals(dictionnaire[i])) {
							// On dit qu'on l'a trouve pour finir la boucle
							trouve = true;
							// On met sa presence à 1 dans le tableau des presences							
							presence[i] = 1;
							// On augmente le nombre de mots trouves
							nbm++;
						}
						i++;
					}
				}
			}

		}catch(Exception e){
			//On affiche l'exception
			e.printStackTrace();
		}finally {
			//fermeture du buffered Reader
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//System.out.println("Termine. Nombre de mots trouvés dans le message : "+nbm);

		return presence;
	}


    /**
	 * 
	 * @param nbham nombre de message ham a apprendre
	 * @param nbspam nombre de message spam a apprendre
	 */
	public static void apprentissage(String dossierApp, int nbspam,int nbham){
		
		System.out.println("Apprentissage sur " + nbspam + " SPAMs et " + nbham + " HAMs...");

		nbSpamApp = nbspam;
		nbHamApp = nbham;

		// parcourt des spam de la base d'apprentissage
		probaPresenceMotSPAM = new double[dictionnaire.length];
		String directory = System.getProperty("user.dir");
		File spamBaseAppDirectory = new File(directory + dossierApp + "/spam/");
		//on recupère le tableau de fichier de spam
		File[] tabSpam=spamBaseAppDirectory.listFiles();
		File fspam;

		for(int j=0;j<nbspam;j++){
			fspam=tabSpam[j];
			//lecture d'un message
			int[] presence = lire_message("" + dossierApp + "/spam/" + fspam.getName());
			for (int i = 0; i < probaPresenceMotSPAM.length; i++) {
				probaPresenceMotSPAM[i] += presence[i];
			}
		}

		for (int i = 0; i < probaPresenceMotSPAM.length; i++) {
			probaPresenceMotSPAM[i] = (probaPresenceMotSPAM[i] + epsilon) / (nbspam + (epsilon + epsilon));
		}
		
		// parcourt des ham de la base d'apprentissage
		probaPresenceMotHAM = new double[dictionnaire.length];
		File hamBaseAppDirectory = new File(directory + dossierApp + "/ham/");
		//on recupère le tableau de fichier de ham
		File[] tabHam=hamBaseAppDirectory.listFiles();
		File fham;
		for(int j=0;j<nbham;j++){
			fham=tabHam[j];
			//lecture d'un message
			int[] presence = lire_message("" + dossierApp + "/ham/" + fham.getName());
			for (int i = 0; i < probaPresenceMotHAM.length; i++) {
				probaPresenceMotHAM[i] += presence[i];
			}
		}

		for (int i = 0; i < probaPresenceMotHAM.length; i++) {
			probaPresenceMotHAM[i] = (probaPresenceMotHAM[i] + epsilon) / (nbham + (epsilon + epsilon));
		}
		
		/**for (int i = 0; i < dictionnaire.length; i++) {
			System.out.println("Le mot " + dictionnaire[i] + " apparait :");
			System.out.println("\t - " + presenceGlobaleSPAM[i] + " fois dans les SPAM");			
			System.out.println("\t - " + presenceGlobaleHAM[i] + " fois dans les HAM");			
		}*/
		
		//Calcul des probabilité a priori : P(Y=SPAM)=(nombre de SPAM)/(nombre dexemple)
		P_Yspam=(double)nbspam/(nbspam+nbham);
		//P(Y=HAM)=1-P(Y=SPAM)
		P_Yham=1.-P_Yspam;

		//System.out.println("proba a priori de spam :"+P_Yspam);
        //System.out.println("proba a priori de ham :"+P_Yham);	
	
	}

	public static void sauvegarde(String classifieur) {
		// Sauvegarde de l'apprentissage dans un fichier
        try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(classifieur));
			
			bw.append("" + nbSpamApp + "\n");
			bw.append("" + nbHamApp + "\n");

            bw.append("" + dictionnaire.length + "\n");

            for (int i = 0; i < dictionnaire.length; i++) {
                bw.append("" + dictionnaire[i] + "\n");
            }

            bw.append("" + P_Yspam + "\n");
            bw.append("" + P_Yham + "\n");

            for (int i = 0; i < probaPresenceMotSPAM.length; i++) {
                bw.append("" + probaPresenceMotSPAM[i] + "\n");
            }

            for (int i = 0; i < probaPresenceMotHAM.length; i++) {
                bw.append("" + probaPresenceMotHAM[i] + "\n");
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}