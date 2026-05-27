package com.moneymate.app.data.local

import com.moneymate.app.data.local.entity.DefaultPerson
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.LoanType

/**
 * Hardcoded initial templates for NLR 1–4.
 * These are inserted once on first launch (if no template exists).
 * After every upload, the template is replaced with the current file snapshot.
 */
object NlrSeedData {

    private fun p(nlr: String, name: String, place: String? = null, idx: Int) =
        DefaultPerson(
            nlrKey = nlr,
            name = name.trim(),
            place = place?.trim(),
            amountGiven = 0.0,
            mode = PaymentMode.CASH,
            sortOrder = idx,
            recordType = LoanType.LENDING,
            isSeeded = true
        )

    val NLR1: List<DefaultPerson> = listOf(
        "Subbamma", "Vanamma", "Engamma", "Srinu", "Srihari", "Basha",
        "Sujatha", "Beebijaan", "Saalman", "Srinu", "Suloxanama",
        "Laxamma", "Chandhini", "Sarmila", "Mumthaj", "Yesdhani", "Varalaxma",
        "Prasanna", "Balakrishna", "Balakottaiya", "Masthanama", "Naresh",
        "Sailaja", "Kumari", "Aruna", "Prasad", "Ravanaiya", "Senchalaxmi",
        "Srisha", "Sridevi", "Sridevi", "Ravanama", "Subbarav", "Surendar",
        "Krishnaveni", "Semalama", "Siva", "Santhi", "Babu", "Radha",
        "Krishtamma", "Keerthi", "Suloxanama", "Ammani", "Ravanamma", "Anitha",
        "Sujatha", "Subamma", "Santhi", "Sravan", "Laxmi", "Kameswarama",
        "Swarupa", "Sureka", "Janaki", "Ceenama", "Laxamma", "Madhavi",
        "Vijiya", "Sridhar", "Chennama", "Susilamma", "Sunitha", "Rajini",
        "Sudharsini", "Madhavi", "Roopa", "Senchamma", "Bavithra", "Pooja",
        "Munilaxmi", "Laxmi", "Srisha", "Sravanthi", "Lavanya", "Padhma",
        "Sujatha", "Suryakumari", "Vijiyamma", "Ramamma", "Haribabu", "VAMSI",
        "Aadhimma", "Subasini", "Vijay", "Vanamma", "Sugunamma", "Ceenama",
        "E.Ravanama", "Kalyani", "Kondamma", "Susilamma", "Sunitha", "Dhurga",
        "Nagaraj", "Kalpana", "Laxmi", "Pushpa", "Ankitha", "Lalithamma",
        "Suganya", "Nagalaxmi", "Harikrishna", "Pallavi", "Kumari", "Sravani",
        "Padhma", "Pushpa", "E.Ravanama", "Lavanya", "Keerthi", "Rathamma",
        "Prasanthi", "Monika", "Sujatha", "Saradha", "Srinu", "Chandhu",
        "Ceenamma", "Govindama", "Jeyalaxmi", "Bavaani", "Ankkama", "Subbamma",
        "Malini", "Anitha", "PADHMA", "Sujatha", "Padhma", "Jaanahi", "Srinu",
        "Pullamma", "Vijayalaxmi", "Alekya", "Vanaja", "Dhayagar", "Dhinesh",
        "Alekya-own", "Vijiyamma", "Sujatha", "Adhimma", "Bavaani", "Semalamma",
        "Gupra", "Sakila", "Bujjamma", "Raasi", "Prasad-hotel", "NLR Kodiswarav",
        "Ravanama", "Hemalatha", "Sumathi", "Padhma"
    ).mapIndexed { i, n -> p("NLR 1", n, null, i) }

    val NLR2: List<DefaultPerson> = listOf(
        "Nagendra", "Sridevi", "Kavitha", "Dharmavathi", "Latha", "Rajini",
        "ERavanama", "Krishnaveni", "Prasanna", "Mamatha", "Bavaani", "Padhma",
        "Laxmi", "Kousalya", "Ceenamma", "Senchamma", "Mounika", "Semala",
        "Senchamma", "Kodiswarama", "Senchramaiya", "Padhma", "Pooja",
        "Jothi", "Senchamma", "Chinnari", "Sandhya", "Reehana", "Munira",
        "Sakira", "Kameswarama", "E.Ravanama", "Padhma", "Enkamma", "Sravani",
        "Dilsath", "Mabujaani", "Masthanama", "Subbamma", "Najimunisa",
        "Hayarunisha", "Gowsinisa", "Sakila", "Jileka", "Jileka", "Nagamma",
        "Anitha", "ERavanamma", "Polamma", "Rajeswari", "Susilamma", "Pradeep",
        "Sugunama", "ERavanama", "Sesamma", "Soundarya", "Aruna", "Usha",
        "Haritha", "Karunya", "Kameswarama", "Engadeswarama", "Sravani",
        "Padhma", "Kumari", "Susilama", "Vamsi", "Manjula", "Saradha",
        "Swarupa", "Saritha", "Dhamodhar", "Sunitha", "Srilatha", "Mythili",
        "Saradha", "Manasha", "Subashini", "Santhi", "Durga", "Rajeswari",
        "Rangamma", "Ashok-madhavi", "Swapna", "Rajeswari", "Padhma",
        "Prasanthi", "Devsenamma", "Swapna", "Swapna-17/07-24", "Subama",
        "Suloxsnama", "E.Ravanaiya", "Rathnama", "Saabira", "Haseena",
        "Anuradha", "Ceenamma", "Rajeswari", "Vijaya", "Srisha", "Alluramma",
        "Senchama", "Bujjamma", "Laxama", "Saroja", "Nagamma", "ERavanaiya",
        "Engadesulu", "Sravani", "Kavalamma", "Saleem", "Sakira", "Saradha",
        "Lalithamma", "ERavanama", "Senchama", "Srisha", "Madhavi", "Lalitha",
        "Subasini", "Ravanaiya", "Masthanama", "Senchaiya", "ERavanama",
        "Ceenaiya", "Laxama", "Pandu", "Supraja", "Kamalamma", "Nancharama",
        "Mamatha", "Kushma", "Hari-uma", "Palamma", "Adhimma", "Vanamma",
        "Enkaiya", "Hymavathi", "Sujitha", "Lavanya", "Mahalaxmi", "Rathnamma",
        "Pujjamma", "Vajrama", "Bujjamma", "Achamma", "Renuka", "Ceenamma",
        "Padhma", "Ravanama", "Chinnamma", "Babu", "Baby", "Saradha",
        "ERavanama", "Kondamma", "Pujjamma", "Padhma", "Laxamma", "Penchilamma",
        "Polamma"
    ).mapIndexed { i, n -> p("NLR 2", n, null, i) }

    val NLR3: List<DefaultPerson> = listOf(
        "Sudhamma", "Kristamma", "Anusiya", "Pavanthi", "Ravanama", "Alavelama",
        "Chinna", "Subamma", "Srisha", "Sravani", "Senchamma", "Laxamma",
        "ERavanama", "ERavanama", "Subasini", "Sesamma", "Sowmiya", "Pramila",
        "Laxamma", "Azrathama", "Laxmi", "Pappaiya", "Ravanamma", "Vijiyama",
        "Nirmalamma", "Kavya", "Uma", "Padhma", "Manjula", "Subramanyam",
        "Pujamma", "Ceenamma", "Sesamma", "Kanthama", "Parvathi", "Ruthama",
        "Supraja", "Nagamani", "Amulya", "Kamatchi", "Palamma", "Devika",
        "Jeyamma", "Subulaxmi", "Jeyalaxmi", "Edukondalama", "Sumalatha",
        "Vaani", "Badhma", "E.Ravanama", "Narendhar", "L.Narasaiya",
        "Mobina-kaja", "Mabujaani", "Basha", "Suganya", "Mamatha", "Mayuri",
        "Venu", "Sridevi", "Srinu", "Ceenaiya", "Padhma", "Ravanamma",
        "Ravanamma", "ERavanama", "Adhiya", "Kala", "Pujjama", "Lavanya",
        "Madhavi", "Srividhya", "Saradha", "Ceenamma", "Siva", "Kondaiya",
        "Meharubasha", "Srilatha", "Kondamma", "Girijama", "Adhilaxma",
        "Seetharavama", "Madhusudhanan", "Rasul", "Prasanna", "Sekar",
        "Vijayalaxmi", "Ravanamma", "Sujatha", "Suguna", "Narasaiya", "Laxmi",
        "Sesamma", "Ceenamma", "Beebijaan", "Mumthaj", "Hameetha", "Badhmavathi",
        "Guljaar", "Ruppa", "Noorjahaan", "Ravanamma", "Saajitha", "Karimulla",
        "Rajeswari", "Guljaar", "Srinivasulu", "Meharuni", "Parvin",
        "Krishnaveni", "Habimunisa", "Mahimunisha", "Noorjahaan", "Jaahitha",
        "Sridevi", "Pravalika", "Jainabee", "Rufiyaa", "Jameer", "Mehathaj",
        "Rijwana", "Nasiba", "Famitha", "Munirjaan", "Shirisha", "Rajeswari",
        "Madhavi", "Kadharunisha", "Rajiya", "Sunitha", "Mahimun", "Habeeba",
        "Sulthanbee", "Parvin", "Makbul", "Sarmila", "Mahabupjaan", "Farjana",
        "Badhullabee", "Nagamani", "Senchaiya", "Saritha", "Makbul", "Makbul",
        "Sesamma", "Laxmi", "Shaila", "Susilama", "Penchilama", "Amulya",
        "ERavanama", "Suguna", "RAFI", "Badhru", "Reehana", "Adhilaxama",
        "Padhma", "Pramila", "Bujjamma", "Rahimunni", "Seethamma", "Kalyani",
        "Kalyani", "Rahimunisa", "Mobina", "Rajiya", "Jemila", "Jemila",
        "Kadharunisa", "Aafrin", "Kadharbee", "Safiya", "Habib", "Nasimunisa",
        "Beebijaan", "Kadharunisa", "Aaliya", "Aaliya", "Noorjahaan", "Naajira",
        "Mujafar", "Mujafar", "Shahith", "Saaliya", "Shanvaas", "Haseena",
        "Katheeja", "Shareef", "AAyisha", "Rahmath", "Neelu", "Suhaana",
        "Hafeeja", "Habeeba", "Habimunisa", "Haseena", "Reena", "Dhisath",
        "Sahitha", "Jeenath-gayas", "Kalam", "Jerina", "RAJIYA", "Aarif",
        "Sakila", "Amrin", "Sabeer", "Sahitha", "Shakir", "Sabeer", "Aashifa",
        "Faathima", "Vohitha", "Jaahitha", "Pappulama", "Engatamma",
        "Meharunisa", "Meharunisa", "Aashifa", "Kajabee", "Thajinni", "Kaaja",
        "Masthanbee", "Mahimunisha", "Akthar", "Vanaja", "Jaakira", "Jothi",
        "Reka", "Srisha", "Penchilama", "Hafeez", "Faaruk", "Mobina",
        "Jerinthaj", "Kadharunisha", "Sarmila", "Aashifa", "Ammani", "Mahimun",
        "Mallika", "Mahimun", "Masthanbee", "Samrin", "Sakila", "Beebijaan",
        "Jeyamma", "Penchilama", "Evanthi", "Ravanama", "Jeyamma", "Maheswari",
        "Saleema", "Parvin", "Masthanbee", "Rafi", "Meharuni", "Sharifa",
        "Subhani", "Karimunisha", "Beebijaan", "Ali", "Ramijun", "Aabitha",
        "Aarifa", "Baseer", "Naajinni", "Habeeb", "Habeeb", "Habeeb",
        "Kadharama", "Esdhani", "Esdhani", "Munni", "Rajiya", "Parvin",
        "Vohitha", "Aasiya", "Shareena", "Aafrin", "Habeebjaan", "Karimunisa",
        "Haseena", "Chinnama", "Mumthaj", "Beebijaan", "Aafrin", "Masthanbee",
        "Jameera", "Laxmi", "Masthanbee", "Rahmath", "Kadarjaan", "Sahitha",
        "Rameeja", "Jakkima", "Ikram", "Gowsiya", "Jaibunisha", "Reshma",
        "Mahimun", "Saathiya", "Rehaana", "Jabeena", "Nowsaadh", "Mallika",
        "Fyroje", "Saahina", "Shilpha", "Prasanna", "Prasanna", "Munikrishna",
        "Bagyama", "Penchilama-Ramu", "Sudhakar reddy", "Parvathi", "Beebijaan",
        "Baseer", "Chinni", "Srihari", "Nanni", "Mumthaj", "Rajiya", "Habeep",
        "Famitha", "Haseena", "Mobina", "Vohitha", "Nowsaadh", "Mallika"
    ).mapIndexed { i, n -> p("NLR 3", n, null, i) }

    val NLR4: List<DefaultPerson> = listOf(
        "EYekon", "Haseena", "Nagbosnama", "Chandeep", "Naresh", "Rajeswari",
        "Anusha", "Radhamma", "Narayanama", "Kaveri", "Ceenaiya", "Naresh",
        "Nagendrama", "ESesamma", "Laxmi", "Subamma", "Gunnama", "Nagaraj",
        "Sai", "Vijayalaxmi", "Shaila", "Suresh", "Senchaiya", "Ceenaiya",
        "Badhma", "Subramani", "Kumari", "Ramesh-bavani", "Sujatha", "Padhma",
        "Srinu", "Haritha", "Ceenama", "Sanjeevama", "Chandhu", "Sunitha",
        "Gowri-nagraj", "Laxmi", "Enkamma", "Sujatha", "Ceenaiya", "Ravanama",
        "Paavani", "Ceenamma", "Laxamma", "E.Ravanama", "Laxmi", "ESesamma",
        "Charan-madhavi", "Kamatchi", "Rajyam", "Genamma", "Poornachandran",
        "Sujatha", "Chandhana", "Amaravathi", "L.Rajyam", "Chinnamma",
        "Sunitha", "Sudhagar", "Giri", "Giri", "Anusha", "Enkamma",
        "Masthanama", "Sujatha", "Swarnalatha", "Latha", "Latha-vijay",
        "L.Narayana", "Sridevi", "Sunitha", "Vasanthama", "Ceenamma", "Madhu",
        "Ravanaiya", "Kristaiya", "Jothi", "Semalama", "Vijiyama", "Masthanama",
        "Nagabosnama", "Kristaiya", "Easwarama", "Nagabosnama", "Srisha",
        "Vanaja", "Enkatamma", "Ramadevi", "ERavanama", "Siva", "Vajramma",
        "Vajramama", "Vajramma", "Vamsi", "Yestherama", "Naveen", "Naveen",
        "Sampoornama", "Edukondalu", "Nagamani", "Rajeswari", "Sumalatha",
        "Rajesh", "Vijayalaxmi", "Sai", "Vinoth", "Nagaraj", "Rama",
        "Murali", "Badhma", "Jeyamma", "Ansuriyamma", "Swapna",
        "Santhakumari", "Vijiyamma", "Saritha", "Sivakumar", "Ramadevi",
        "Swarthamma", "Radha", "Merry", "Vijayprabha", "Rasul", "RAMESH"
    ).mapIndexed { i, n -> p("NLR 4", n, null, i) }

    val ALL = NLR1 + NLR2 + NLR3 + NLR4
}