#!/usr/bin/env ruby

require 'net/http'
require 'uri'

bodies = ["""
Look, just because I don't be givin' no man a foot massage don't make it right for Marsellus to throw Antwone into a glass motherfuckin' house, fuckin' up the way the nigger talks. Motherfucker do that shit to me, he better paralyze my ass, 'cause I'll kill the motherfucker, know what I'm sayin'?
""","""
My money's in that office, right? If she start giving me some bullshit about it ain't there, and we got to go someplace else and get it, I'm gonna shoot you in the head then and there. Then I'm gonna shoot that bitch in the kneecaps, find out where my goddamn money is. She gonna tell me too. Hey, look at me when I'm talking to you, motherfucker. You listen: we go in there, and that nigga Winston or anybody else is in there, you the first motherfucker to get shot. You understand?
""","""
You think water moves fast? You should see ice. It moves like it has a mind. Like it knows it killed the world once and got a taste for murder. After the avalanche, it took us a week to climb out. Now, I don't know exactly when we turned on each other, but I know that seven of us survived the slide... and only five made it out. Now we took an oath, that I'm breaking now. We said we'd say it was the snow that killed the other two, but it wasn't. Nature is lethal but it doesn't hold a candle to man.
""","""
Well, the way they make shows is, they make one show. That show's called a pilot. Then they show that show to the people who make shows, and on the strength of that one show they decide if they're going to make more shows. Some pilots get picked and become television programs. Some don't, become nothing. She starred in one of the ones that became nothing.
""","""
The path of the righteous man is beset on all sides by the iniquities of the selfish and the tyranny of evil men. Blessed is he who, in the name of charity and good will, shepherds the weak through the valley of darkness, for he is truly his brother's keeper and the finder of lost children. And I will strike down upon thee with great vengeance and furious anger those who would attempt to poison and destroy My brothers. And you will know My name is the Lord when I lay My vengeance upon thee.
""","""
Do you see any Teletubbies in here? Do you see a slender plastic tag clipped to my shirt with my name printed on it? Do you see a little Asian child with a blank expression on his face sitting outside on a mechanical helicopter that shakes when you put quarters in it? No? Well, that's what you see at a toy store. And you must think you're in a toy store, because you're here shopping for an infant named Jeb.
""","""
Normally, both your asses would be dead as fucking fried chicken, but you happen to pull this shit while I'm in a transitional period so I don't wanna kill you, I wanna help you. But I can't give you this case, it don't belong to me. Besides, I've already been through too much shit this morning over this case to hand it over to your dumb ass.
""","""
Look, just because I don't be givin' no man a foot massage don't make it right for Marsellus to throw Antwone into a glass motherfuckin' house, fuckin' up the way the nigger talks. Motherfucker do that shit to me, he better paralyze my ass, 'cause I'll kill the motherfucker, know what I'm sayin'?
""","""
You think water moves fast? You should see ice. It moves like it has a mind. Like it knows it killed the world once and got a taste for murder. After the avalanche, it took us a week to climb out. Now, I don't know exactly when we turned on each other, but I know that seven of us survived the slide... and only five made it out. Now we took an oath, that I'm breaking now. We said we'd say it was the snow that killed the other two, but it wasn't. Nature is lethal but it doesn't hold a candle to man.
"""]

titles = """
Access
Achebe
Achelous
Achilles
Acrobat
Adam II
Adam X
Adaptoid
Administrator
Adonis
Adrenazon
Adversary
Advisor
Aegis
Aero
Afari, Jamal
Aftershock
Agamemnon
Agamotto
Aged Genghis
Agent
Agent Axis
Agent Cheesecake
Agent X
Agent Zero
Aginar
Aggamon
Agon
Agron
Agony
El Aguila
Aguja
Ahab
Ahura
Air-Walker
Airborne
Aireo
Airstrike
Ajak
Ajax
Ajaxis
Akasha
Akhenaten
A'lars
Alaris
Albert
Albino
Albion
Alchemy
Alcmena
Aldebron
Alex
Alexander, Caleb
Alexander, Carrie
Algrim the Strong
Alhazred, Abdul
Alibar
Alistair Smythe
Alistaire Stuart
Aliyah Bishop
Alkhema
All-American
Allan, Liz
Allatou
Allerdyce, St. John
Alpha Ray
Alpha the Ultimate Mutant
Alraune, Marlene
Alysande Stuart
Alyssa Moy
Amalgam
Amanda Sefton
Amatsu-Mikaboshi
Amazon
Amber Hunt
Amelia Voght
Amergin
American Ace
American Dream
American Eagle
American Samurai
Americop
Ameridroid
Amiko Kobayashi
Amina Synge
Aminedi
Ammo
Amphibian
Amphibion
Amphibius
Amun
Anaconda
Anais
Analyzer
Anarchist
Ancient One
Anderssen, Tanya
Andreas von Strucker
Andrew Chord
Andrew Gervais
Android Man
Andromeda
Anelle
Angar the Screamer
The Angel
Angel
Angel Dust
Angel Face
Angel Salvadore
Angela Cairn
Angela Del Toro
Angler
Ani-Mator
Animus
Animus
Ankhi
Annalee
Anelle
Anne-Marie Cortez
Annex
Annie Ghazikhanian
Annihilus
""".split("\n")


uri = URI.parse("http://localhost:9000/old")
response = Net::HTTP.post_form(uri, 
  {
    "title" => titles[Random.rand(titles.length)],
    "body" => bodies[Random.rand(bodies.length)]
  })



