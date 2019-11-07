#Get the Feature Data
filepath <- "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\40178464_features.csv"
feature_col_names <- c("label", "index", "nr_pix", "height", "width", "span", "rows_wth_5", "cols_with_5", "neigh1", "neigh5", 
                       "left2tile", "right2tile", "verticalness", "top2tile", "bottom2tile", "horizontalness", "3tile1", "3tile2", 
                       "nr_regions", "nr_eyes", "hollowness", "image_fill")

feature_data <- read.csv(filepath, header = TRUE, sep = "\t", col.names = feature_col_names)


#Get the Feature Data for Living Things
living_things_names <- c("Banana", "Cherry", "Flower", "Pear")
living_thing_data <- feature_data[which(feature_data$label %in% living_things_names), 1:22]


#Get the Feature Data for Nonliving Things
nonliving_things_names <- c("Envelope", "Golfclub", "Pencil", "Wineglass")
nonliving_thing_data <- feature_data[which(feature_data$label %in% nonliving_things_names), 1:22]

#1. Construct Histograms for nr_pix, height, cols_with_5 for different feature sets

#Living Things
hist(living_thing_data$nr_pix, main = "Living Things - No. of Pixels")
hist(living_thing_data$height, main = "Living Things - Height")
hist(living_thing_data$cols_with_5, main = "Living Things - Columns with 5+")

#Nonliving Things
hist(nonliving_thing_data$nr_pix, main = "Nonliving Things - No. of Pixels")
hist(nonliving_thing_data$height, main = "Nonliving Things - Height")
hist(nonliving_thing_data$cols_with_5, main = "Nonliving Things - Columns with 5+")

#Full Feature Set
hist(feature_data$nr_pix, main = "Entire Set - No. of Pixels")
hist(feature_data$height, main = "Entire Set - Height")
hist(feature_data$cols_with_5, main = "Entire Set - Columns with 5+")

#2. Summary Statistics



#3. Plot theoretical Normal Distribution for nr_pix
variance_nr_pix <- var(feature_data$nr_pix)
plotx_nr_pix <- seq(0, 250, 25)
pnorm_nr_pix <- dnorm(plotx_nr_pix, mean_nr_pix, sqrt(variance_nr_pix))
plot(plotx_nr_pix, pnorm_nr_pix, main = "Normal Distribution for No. of Pixels")

