# Demographic language parser

This program parses a file describing the demographic history of one or more populations with migration. It stores a demographic model in memory that can be queries for several quantities, including coalescent rate, expected fraction of genome or number of segments shared IBD within or across populations. Populations may change size over time and interact through migration.

This is work in progress and much cleaning is needed.

Contact: ppalama AT hsph DOT harvard DOTAGAIN edu

References for the IBD computations:
- P. F. Palamara, T. Lencz, A. Darvasi, I. Pe'er. "Length distributions of identity by descent reveal fine-scale demographic history". The American Journal of Human Genetics, 2012. 
- P. F. Palamara, I. Pe'er. "Inference of historical migration rates via haplotype sharing". Bioinformatics, 2013.
