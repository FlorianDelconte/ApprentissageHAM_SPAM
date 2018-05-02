import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class filtreAntiSpam {
	private final static String regex = "[\\s\\<\\>\\[\\]\\,\\?\\;\\.\\:\\/\\!\\§\\*\\µ\\$\\£\\^\\¨\\%\\@\\)\\(\\=\\+\\{\\'\\}\\|\\#\\-\\\"\\.\\_\\`\\[\\]\\&\\°\\\\[0-9]]+";

	//c'est mieux un tableau plutot qu'une liste car on vas faire beaucoup d'accès au tableau
	private static String[] dictionnaire;
	// retient, pour chaque mot du dictionnaire, le nombre de fichier où il est présent
	private static int[] presenceGlobaleSPAM;
	private static int[] presenceGlobaleHAM;	
	//probabilité a priori
	private static double P_Yspam;
	private static double P_Yham;

	public static void main(String[] args) {
		//on charge le dictionnaire
		charger_dictionnaire();
		//saisi clavier du nombre de spam a apprendre
		Scanner sc= new Scanner(System.in);
		System.out.println("combien de SPAM dans la base d'apprentissage ? ");
		int nbspam=sc.nextInt();
		//saisi clavier du nombre de ham a apprendre
		System.out.println("combien de HAM dans la base d'apprentissage ? ");
		int nbham=sc.nextInt();
		
		
		apprentissage(nbspam,nbham);
		test(500,500,nbspam,nbham);
		
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
		
		/*** MODIF ***/

		ArrayList<String> dictionnaireListe = new ArrayList<>();

		/*************/

		try{

			br = new BufferedReader(new FileReader(fileName));

			int i=0;

			//on parcourt jusqu'a la fin du fichier pour compter le nombre de ligne -> permet d'avoirs la taille du tableau dictionnaire
			while ((ligne = br.readLine()) != null) {
				if(ligne.trim().length()>=3){
					i++;

					/*** MODIF ***/

					dictionnaireListe.add(ligne.toUpperCase().trim());

					/*************/
				}
			}

			/*** MODIF ***/

			dictionnaire = new String[dictionnaireListe.size()];
			dictionnaire = dictionnaireListe.toArray(dictionnaire);

			/*************/

			/*
			//initialisation du tableau
			dictionnaire=new String[i];
			//on remet a 0 le compteur et on réinitialise le buffredReader
			i=0;
			br = new BufferedReader(new FileReader(fileName));
			//on parcourt jusqu'a la fin du fichier et on ajouter chaque ligne au dico
			while ((ligne = br.readLine()) != null) {
				
				//on recupère les mots supérieur a 2 lettres
				if(ligne.length()>=3){
					//On ajoute le mot au dico
					dictionnaire[i]=ligne;
					//on passe la case suivante du tableau
					i++;
				}
			}
			*/

			// avec modif : 6 milliseconds pour dico à 950 mots
			// sans modif : 8 milliseconds ____________________

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

			// PREMIERE VERSION : on prend chaque mot du dictionnaire
			// 					et on regarde si il est dans le fichier

			/*
			//mot a rechercher
			String motARechercher;
			//D'abord on parcout le dictionnaire
			for(int i=0;i<dictionnaire.length;i++){
				//on recupère le mot du dictionnaire qu'on cherche
				motARechercher=dictionnaire[i];

				//enssuite on parcourt tout le fichier pour trouver le mot
				//TODO : parcourir jusqu'a ce qu'on a trouvé le mot
				while ((ligne = br.readLine()) != null ) {
					
					//TODO :C'est pas ouf comme condition je crois faut changer ça pour mieux reperer les mots
					//si la ligne en majuscule contient le mot		
					if(ligne.toUpperCase().contains(dictionnaire[i])){
						//on met à 1 le tableau de presence a la postion du mot du dictionnaire 
						presence[i]=1;
						//on incremente le nombre de mot trouvé 
						nbm++;
					}
				} 
			}
			*/
			

			// DEUXIEME VERSION : on parcourt le fichier, et on regarde
			// 					si chaque mot est dans le dictionnaire
			
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

			// avec première version : environ 25 millisecondes mais 6 mots trouvés
			// avec deuxième version : environ 35 millisecondes mais 60 mots trouvés

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
	public static void apprentissage(int nbspam,int nbham){
		
		System.out.println("Apprentissage...");

		// parcourt des spam de la base d'apprentissage
		presenceGlobaleSPAM = new int[dictionnaire.length];
		String directory = System.getProperty("user.dir");
		File spamBaseAppDirectory = new File(directory + "/base/baseapp/spam/");
		//on recupère le tableau de fichier de spam
		File[] tabSpam=spamBaseAppDirectory.listFiles();
		File fspam;
		/**MODIF**/
		for(int j=0;j<nbspam;j++){
			fspam=tabSpam[j];
			//lecture d'un message
			int[] presence = lire_message("/base/baseapp/spam/" + fspam.getName());
			for (int i = 0; i < presenceGlobaleSPAM.length; i++) {
				presenceGlobaleSPAM[i] += presence[i];
			}
		}
		/*********/
		
		/*for (File f : spamBaseAppDirectory.listFiles()) {
			System.out.println(tabFile[20]);
			//lecture d'un message
			int[] presence = lire_message("/base/baseapp/spam/" + f.getName());

			for (int i = 0; i < presenceGlobaleSPAM.length; i++) {
				presenceGlobaleSPAM[i] += presence[i];
			}
		}*/
		
		// parcourt des ham de la base d'apprentissage
		presenceGlobaleHAM = new int[dictionnaire.length];
		File hamBaseAppDirectory = new File(directory + "/base/baseapp/ham/");
		//on recupère le tableau de fichier de ham
		File[] tabHam=hamBaseAppDirectory.listFiles();
		File fham;
		for(int j=0;j<nbham;j++){
			fham=tabHam[j];
			//lecture d'un message
			int[] presence = lire_message("/base/baseapp/ham/" + fham.getName());
			for (int i = 0; i < presenceGlobaleHAM.length; i++) {
				presenceGlobaleHAM[i] += presence[i];
			}
		}
		/*for (File f : spamBaseAppDirectory.listFiles()) {
			//lecture d'un message
			int[] presence = lire_message("/base/baseapp/ham/" + f.getName());

			for (int i = 0; i < presenceGlobaleHAM.length; i++) {
				presenceGlobaleHAM[i] += presence[i];
			}
		}*/
		 
		/**for (int i = 0; i < dictionnaire.length; i++) {
			System.out.println("Le mot " + dictionnaire[i] + " apparait :");
			System.out.println("\t - " + presenceGlobaleSPAM[i] + " fois dans les SPAM");			
			System.out.println("\t - " + presenceGlobaleHAM[i] + " fois dans les HAM");			
		}*/
		
		//Calcul des probabilité a priori : P(Y=SPAM)=(nombre de SPAM)/(nombre dexemple)
		P_Yspam=(double)nbspam/(nbspam+nbham);
		//P(Y=HAM)=1-P(Y=SPAM)
		P_Yham=1-P_Yspam;
		//System.out.println("proba a priori de spam :"+P_Yspam);
		//System.out.println("proba a priori de ham :"+P_Yham);
	}
	/**
	 * 
	 * @param nbSpamApp nombre de spam de la base apprentissage
	 * @param nbHamApp nombre de ham de la base Apprentissage
	 * @return true si c'est un SPAM
	 */
	public static boolean testMessageX(int nbSpamApp,int nbHamApp, int[] presence){
		//le nouvel email est-il un SPAM ?
		//pour le calcul des probabilité a Posteriori on a besoin de : P(X=x)=P(X=x|Y=SPAM)P(Y=SPAM)+P(X=x|Y=HAM)P(Y=HAM) (--->Formule des proba total)
		// P(X=x|Y=SPAM)=produit de Bspam par Bham (sur j allant de 1 à nb nbspam)-->voir p52
		// P(X=x|Y=HAM)=produit de Bspam par Bham (sur j allant de 1 à nb nbham)-->voir p52
		//Le truc c'est que si on fait le produit de truc qui se rapporche beaucoup de 0 java vas simplifier en faisant 0*0=0 la tete a toto quoi
		//Solution: P(X=x|Y=SPAM)=LOG(produit de Bspam par Bham )(sur j allant de 1 à nb nbspam)
		//ET   P(X=x|Y=HAM)=LOG(produit de Bspam par Bham )(sur j allant de 1 à nb nbham)--> logarithme d'un produit= somme des logarithme
		//DONC : 	P(X=x|Y=SPAM)=LOG( Bspam)+ LOG(Bham) (sur j allant de 1 à nb nbspam)
		//ET   		P(X=x|Y=HAM)=LOG(Bspam) + LOG(Bham)(sur j allant de 1 à nb nbham)
		boolean b;
		double P_Xx_YSPAM=0;	
		for(int j=0;j<presence.length;j++){
			//Si le mot est j est présent dans le fichier spam
			if(presence[j]==1){
				//On ajoute à P(X=x|Y=SPAM) le log de bjspam
				P_Xx_YSPAM+=Math.log((double)(presenceGlobaleSPAM[j]+1)/(nbSpamApp+2));
			//Si le mot j n'est pas présent dans le fichier spam
			}else{
				//On ajoute à P(X=x|Y=SPAM) le log de 1-bjspam
				P_Xx_YSPAM+=Math.log(1.-((double)(presenceGlobaleSPAM[j]+1)/(nbSpamApp+2)));
			}
		}
		P_Xx_YSPAM+=Math.log(P_Yspam);
	//	System.out.println(P_Xx_YSPAM);
		double P_Xx_YHAM=0;

		for(int j=0;j<presence.length;j++){
			//Si le mot est j est présent dans le fichier ham
			if(presence[j]==1){
				//On ajoute à P(X=x|Y=HAM) le log de bjham
				P_Xx_YHAM+=Math.log((double)(presenceGlobaleHAM[j]+1)/(nbHamApp+2));
			//Si le mot j n'est pas présent dans le fichier spam
			}else{
				//On ajoute à P(X=x|Y=HAM) le log de 1-bjham
				P_Xx_YHAM+=Math.log(1.-((double)(presenceGlobaleHAM[j]+1)/(nbHamApp+2)));
			}
		}
		P_Xx_YHAM+=Math.log(P_Yham);
	//	System.out.println(P_Xx_YHAM);
		if(P_Xx_YSPAM>P_Xx_YHAM){
			b=true;
		}else{
			b=false;
		}
		return b;
		
	}
	public static void test(int nbSpamTest,int nbHamTest,int nbSpamApp,int nbHamApp){
		String directory = System.getProperty("user.dir");
		File hamBaseTestDirectory = new File(directory + "/base/basetest/ham/");
		File[] tabHam=hamBaseTestDirectory.listFiles();
		File fham;
		System.out.println("TEST :");
		//TEST de HAM
		int errHam=0;
		for(int i=0;i<nbHamTest;i++){
			fham=tabHam[i];
			//lecture d'un message dans la base de test
			int[] presence = lire_message("/base/basetest/ham/" + fham.getName());
			if(testMessageX(nbSpamApp,nbHamApp,presence)){
				System.out.println("HAM numéro "+i+" identifié comme un SPAM ***erreur***");
				errHam++;
			}else{
				System.out.println("HAM numéro "+i+" identifié comme un HAM");
			}
		}
		//TEST de SPAM
		int errSpam=0;
		File spamBaseTestDirectory = new File(directory + "/base/basetest/spam/");
		File[] tabSpam=spamBaseTestDirectory.listFiles();
		File fspam;
		for(int i=0;i<nbSpamTest;i++){
			fspam=tabSpam[i];
			//lecture d'un message dans la base de test
			int[] presence = lire_message("/base/basetest/spam/" + fspam.getName());
			if(testMessageX(nbSpamApp,nbHamApp,presence)){
				System.out.println("SPAM numéro "+i+" identifié comme un SPAM");
			}else{
				System.out.println("SPAM numéro "+i+"identifié comme un HAM  ***erreur***");
				errSpam++;
			}
		}
		System.out.println("Erreur de test sur les "+nbHamTest+" HAM : "+((double)errHam/nbHamTest)*100);
		System.out.println("Erreur de test sur les "+nbSpamTest+" SPAM : "+((double)errSpam/nbSpamTest)*100);
		int nbHamSpamTestTotal=nbHamTest+nbSpamTest;
		int errTotal=errHam+errSpam;
		System.out.println("Erreur de test sur les "+nbHamSpamTestTotal+" mails : "+((double)errTotal/nbHamSpamTestTotal)*100);
	}
	
}
