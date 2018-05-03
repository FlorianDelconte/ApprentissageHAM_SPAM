import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.IOException;

public class apprend_filtre_enligne {

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
        String directory = System.getProperty("user.dir");

        // On remplit les valeurs en attribut à partir d'un classifieur
        chargement_classifieur("" + directory + "/" + args[0]);

        // On recalcule les valeurs d'un des deux tableaux de probabilités
        // Si le parametre est SPAM, met à jour le tableau probaPresenceMotSPAM
        // sinon met à jour le tableau probaPresenceMotHAM
        recalcul_valeurs("/" + args[1], args[2].equals("SPAM"));

        // On sauvegarde dans le fichier classifieur passe en parametre
        sauvegarde("" + directory + "/" + args[0]);

        System.out.println("Modification du filtre '" + args[0] + "' par apprentissage sur le " + args[2] + " '" + args[1] + "'.");

    }

    public static void chargement_classifieur(String fichier) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fichier));
			
			nbSpamApp = Integer.parseInt(br.readLine());
			nbHamApp = Integer.parseInt(br.readLine());

			tailleDictionnaire = Integer.parseInt(br.readLine());

			dictionnaire = new String[tailleDictionnaire];

			for (int i = 0; i < tailleDictionnaire; i++) {
				dictionnaire[i] = br.readLine();
			}
			
			P_Yspam = Double.parseDouble(br.readLine());
			P_Yham = Double.parseDouble(br.readLine());

			probaPresenceMotSPAM = new double[tailleDictionnaire];
			probaPresenceMotHAM = new double[tailleDictionnaire];

			for (int i = 0; i < tailleDictionnaire; i++) {
				probaPresenceMotSPAM[i] = Double.parseDouble(br.readLine());
			}

			for (int i = 0; i < tailleDictionnaire; i++) {
				probaPresenceMotHAM[i] = Double.parseDouble(br.readLine());
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void recalcul_valeurs(String nomFichier, boolean estUnSpam) {
        int[] presence = lire_message(nomFichier);

        if (estUnSpam) {
            for (int i = 0; i < probaPresenceMotSPAM.length; i++) {
                // On multiplie la proba par son dénominateur (mSPAM + 2epsilon dans le sujet) pour retrouver le numérateur (qu'on arrondit), on lui ajoute la valeur de presence (0 ou 1)
                // Puis on divise par le nouveau dénominateur (mSPAM + 1 + 2epsilon dans le sujet)
                probaPresenceMotSPAM[i] = (double)(Math.round(probaPresenceMotSPAM[i] * (nbSpamApp + (epsilon + epsilon))) + presence[i]) / (nbSpamApp + 1 + (epsilon + epsilon));
            }

            // On augmente le nombre de spams traités
            nbSpamApp++;
        } else {
            for (int i = 0; i < probaPresenceMotHAM.length; i++) {
                // On multiplie la proba par son dénominateur (mSPAM + 2epsilon dans le sujet) pour retrouver le numérateur (qu'on arrondit), on lui ajoute la valeur de presence (0 ou 1)
                // Puis on divise par le nouveau dénominateur (mSPAM + 1 + 2epsilon dans le sujet
                probaPresenceMotHAM[i] = (double)(Math.round(probaPresenceMotHAM[i] * (nbHamApp + (epsilon + epsilon))) + presence[i]) / (nbHamApp + 1 + (epsilon + epsilon));
            }

            // On augmente le nombre de hams traités
            nbHamApp++;
        }

        //Calcul des probabilité a priori : P(Y=SPAM)=(nombre de SPAM)/(nombre dexemple)
		P_Yspam=(double)nbSpamApp/(nbSpamApp+nbHamApp);
		//P(Y=HAM)=1-P(Y=SPAM)
        P_Yham=1.-P_Yspam;
        

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
		int [] presence=new int[tailleDictionnaire];
		//chemin du repertoire du projet
		String fichier = System.getProperty("user.dir");
		fichier+=nomFichier;
		//Buffered reader
		BufferedReader br = null;
		//la ligne courante
		String ligne;
		try{

			br = new BufferedReader(new FileReader(fichier));

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
					while (!trouve && i < tailleDictionnaire) {
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